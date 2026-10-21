/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.cli;

import org.aesh.command.Command;
import org.aesh.command.CommandException;
import org.aesh.command.CommandResult;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.cli.cmd.CommandDomain;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.LatestVersionNotAvailableException;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class PmSessionCommand implements Command<PmCommandInvocation>, GalleonCLICommand {

    @Override
    public CommandResult execute(PmCommandInvocation session) throws CommandException {
        GalleonCommandExecutionContext ctx = null;
        try {
            session.getPmSession().commandStart(session);
            if(getCommandClassName(session.getPmSession()) != null) {
                ctx = getGalleonContext(session.getPmSession());
                if (ctx == null) {
                    String version = getCoreVersion(session.getPmSession());
                    ctx = session.getPmSession().getGalleonContext(version);
                }
                ctx.execute(this, session);
            } else {
                runCommand(session);
            }
            return CommandResult.SUCCESS;
        } catch (Throwable t) {
            handleException(ctx, session, t);
            return CommandResult.FAILURE;
        } finally {
            session.getPmSession().commandEnd(session);
        }
    }
    public static void handleException(PmCommandInvocation session, Throwable t) throws CommandException {
        handleException(null, session, t);
    }
    public static void handleException(GalleonCommandExecutionContext ctx, PmCommandInvocation session, Throwable t) throws CommandException {
        if (session.getPmSession().isExceptionRethrown()) {
            throw new CommandException(t);
        }
        //t.printStackTrace();
        printException(ctx, session.getPmSession(), t);
    }

    static void printException(GalleonCommandExecutionContext ctx, PmSession session, Throwable t) {
        if (t instanceof RuntimeException) {
            CliLogging.exception(t);
            t.printStackTrace(session.getErr());
        }
        t = handleCommandExecutionException(ctx, t);
        CliLogging.error(t.getMessage());
        session.print("Error: ");
        println(session, t);

        t = t.getCause();
        int offset = 1;
        while (t != null) {
            for (int i = 0; i < offset; ++i) {
                session.print(" ");
            }
            session.print("* ");
            println(session, t);
            t = t.getCause();
            ++offset;
        }
    }

    private static Throwable handleCommandExecutionException(GalleonCommandExecutionContext ctx, Throwable t) {
        if (t instanceof CommandExecutionException) {
            CommandExecutionException cex = (CommandExecutionException) t;
            if (cex.getPmSession() != null) {
                // Handle default and named universes
                if (cex.getCause() instanceof LatestVersionNotAvailableException) {
                    LatestVersionNotAvailableException cause = (LatestVersionNotAvailableException) cex.getCause();
                    FeaturePackLocation fpl = ctx.getExposedLocation(null, cause.getLocation());
                    t = new LatestVersionNotAvailableException(fpl);
                }
            }
        }
        return t;
    }

    private static void println(PmSession session, Throwable t) {
        if(t.getLocalizedMessage() == null) {
            session.println(t.getClass().getName());
        } else {
            session.println(t.getLocalizedMessage());
        }
    }

    protected String getCoreVersion(PmSession session) throws ProvisioningException {
        return null;
    }
    @Override
    public String getCommandClassName(PmSession session) throws ProvisioningException {
        return null;
    }

    public GalleonCommandExecutionContext getGalleonContext(PmSession session) throws CommandException {
        return null;
    }

    protected abstract void runCommand(PmCommandInvocation session) throws CommandExecutionException;

    public CommandDomain getDomain() {
        // null is the default value for child commands.
        // Unit test check that top level and parent commands have a proper domain set.
        return null;
    }
}
