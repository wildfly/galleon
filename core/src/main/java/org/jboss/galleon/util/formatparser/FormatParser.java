/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.galleon.util.formatparser;

import java.util.ArrayList;
import java.util.List;

import org.jboss.galleon.util.formatparser.formats.WildcardParsingFormat;
import org.jboss.galleon.util.formatparser.formats.expr.FormatExprContentHandler;
import org.jboss.galleon.util.formatparser.formats.expr.FormatExprParsingFormat;
import org.jboss.galleon.util.formatparser.formats.expr.FormatExprTypeParamContentHandler;
import org.jboss.galleon.util.formatparser.formats.expr.FormatExprTypeParamParsingFormat;

/**
 *
 * @author Alexey Loubyansky
 */
public class FormatParser implements ParsingContext {

    public static ParsingFormat resolveFormat(String expr) throws FormatParsingException {
        return (ParsingFormat) parse(
                ExtendedContentHandlerFactory.getInstance()
                .addContentHandler(FormatExprParsingFormat.NAME, FormatExprContentHandler.class)
                .addContentHandler(FormatExprTypeParamParsingFormat.NAME, FormatExprTypeParamContentHandler.class)
                .addContentHandler(FormatExprParsingFormat.LIST_TYPE_FORMAT_NAME, FormatExprParsingFormat.ListTypeContentHandler.class)
                .addContentHandler(FormatExprParsingFormat.COMPOSITE_TYPE_FORMAT_NAME, FormatExprParsingFormat.CompositeTypeContentHandler.class),
                FormatExprParsingFormat.getInstance(), expr);
    }

    public static Object parse(String str) throws FormatParsingException {
        return parse(DefaultContentHandlerFactory.getInstance(), WildcardParsingFormat.getInstance(), str);
    }

    public static Object parse(ParsingFormat format, String str) throws FormatParsingException {
        return parse(DefaultContentHandlerFactory.getInstance(), format, str);
    }

    public static Object parse(FormatContentHandlerFactory cbFactory, ParsingFormat format, String str) throws FormatParsingException {
        return new FormatParser(cbFactory, format, str).parse();
    }

    private final ParsingFormat rootFormat;
    private final FormatContentHandlerFactory cbFactory;

    private List<FormatContentHandler> cbStack = new ArrayList<>();

    private String str;
    private int chI;
    private int handlerIndex;

    private boolean breakHandling;
    private boolean bounced;

    public FormatParser(FormatContentHandlerFactory cbFactory, ParsingFormat rootFormat, String str) {
        this.rootFormat = rootFormat;
        this.cbFactory = cbFactory;
        this.str = str;
    }

    public Object parse() throws FormatParsingException {
        if(str == null) {
            return null;
        }

        chI = 0;

        final FormatContentHandler rootCb = cbFactory.forFormat(rootFormat, chI);
        if (!str.isEmpty()) {
            cbStack.add(rootCb);
            try {
                doParse();
            } catch(FormatParsingException e) {
                final ParsingFormat format;
                final int formatStart;
                if(handlerIndex < 0) {
                    format = rootFormat;
                    formatStart = 0;
                } else {
                    FormatContentHandler ch = cbStack.get(handlerIndex);
                    format = ch.format;
                    formatStart = ch.strIndex;
                }
                throw new FormatParsingException(FormatErrors.parsingFailed(str, chI, format, formatStart), e);
            }
        }
        return rootCb.getContent();
    }

    private void doParse() throws FormatParsingException {
        rootFormat.pushed(this);

        while (++chI < str.length()) {

            handlerIndex = cbStack.size();
            breakHandling = false;
            bounced = false;
            while (handlerIndex > 0 && !breakHandling) {
                final FormatContentHandler cb = cbStack.get(--handlerIndex);
                cb.getFormat().react(this);
            }

            if (bounced || !breakHandling) {
                handlerIndex = cbStack.size() - 1;
                if(handlerIndex < 0) {
                    throw new FormatParsingException("EOL");
                }
//                if(bounced) {
//                    System.out.println(charNow() + " bounced to " + cbStack.get(formatIndex).getFormat());
//                }
                cbStack.get(handlerIndex).getFormat().deal(this);
            }
        }

        if (handlerIndex >= 0) {
            FormatContentHandler ended = cbStack.get(handlerIndex--);
            ended.getFormat().eol(this);
            while (handlerIndex >= 0) {
                cbStack.get(handlerIndex).addChild(ended);
                ended = cbStack.get(handlerIndex--);
                ended.getFormat().eol(this);
            }
        }
    }

    @Override
    public void pushFormat(ParsingFormat format) throws FormatParsingException {
        if(handlerIndex != cbStack.size() - 1) {
            final StringBuilder buf = new StringBuilder();
            buf.append(cbStack.get(0).getFormat());
            if(handlerIndex == 0) {
                buf.append('!');
            }
            for(int i = 1; i < cbStack.size(); ++i) {
                buf.append(", ").append(cbStack.get(i).getFormat());
                if(handlerIndex == i) {
                    buf.append('!');
                }
            }
            throw new FormatParsingException("Child formats need to be popped: " + buf);
        }
        breakHandling = true;
        cbStack.add(cbFactory.forFormat(format, chI));
        //System.out.println("pushFormat: " + format + " [" + cbStack.get(formatIndex).getFormat() + ", " + charNow() + "]");
        ++handlerIndex;
        format.pushed(this);
    }

    @Override
    public void popFormats() throws FormatParsingException {
        breakHandling = true;
        if(handlerIndex == cbStack.size() - 1) {
            return;
        }
        for(int i = cbStack.size() - 1; i > handlerIndex; --i) {
            final FormatContentHandler ended = cbStack.remove(i);
            //System.out.println("poppedFormat: " + ended.getFormat());
            if(!cbStack.isEmpty()) {
                cbStack.get(i - 1).addChild(ended);
            }
        }
    }

    @Override
    public void end() throws FormatParsingException {
        breakHandling = true;
        --handlerIndex; // this is done before the loop for correct error reporting
        for(int i = cbStack.size() - 1; i >= handlerIndex + 1; --i) {
            final FormatContentHandler ended = cbStack.remove(i);
            if(!cbStack.isEmpty()) {
                cbStack.get(i - 1).addChild(ended);
            }
        }

        if(!cbStack.isEmpty() && cbStack.get(handlerIndex).format.isWrapper()) {
            while (handlerIndex > 0) {
                final FormatContentHandler ended = cbStack.get(handlerIndex);
                if(!ended.format.isWrapper()) {
                    break;
                }
                cbStack.remove(handlerIndex--);
                cbStack.get(handlerIndex).addChild(ended);
            }
        }

        if(cbStack.isEmpty() && chI < str.length() - 1) {
            throw new FormatParsingException(FormatErrors.formatEndedPrematurely(rootFormat));
        }
    }

    @Override
    public void bounce() {
        breakHandling = true;
        bounced = true;
    }

    @Override
    public char charNow() {
        return str.charAt(chI);
    }

    @Override
    public boolean startsNow(String str) {
        return this.str.startsWith(str, chI);
    }

    @Override
    public void content() throws FormatParsingException {
        cbStack.get(cbStack.size() - 1).character(charNow());
    }
}
