package com.abctelecom.nms.controller;

import com.abctelecom.nms.view.Launcher;

/**
 * Thin MVC controller entry point. The Swing controllers live with their
 * panels (event handlers delegate to services); this class exists so the
 * documented launch command `java com.abctelecom.nms.controller.AppController`
 * also works and to make the controller layer explicit.
 */
public final class AppController {
    public static void main(String[] args) {
        Launcher.main(args);
    }
}
