/* This file is part of the GameBoyLFB project.
   GameBoyLFB - A Java Game Boy emulator.
   Copyright (C) 2015 Alex Reisner <thearcher at gmail dot com>,
   Stephen Thompson <stephen at chomadoma dot net>

   This project is licensed under the GNU GPL v2 license and comes with
   absolutely no warranty of any kind. The full license can be found at:
   http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt */

package com.github.reisnera.gameboylfb;

import java.util.logging.Logger;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;

import java.nio.ByteBuffer;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.MemoryUtil;

public class App {
    private static final Logger log = Logger.getLogger("Main Log");
    private GLFWErrorCallback errorCallback;
    private GLFWKeyCallback keyCallback;
    private long window;
    private int WIDTH = 160;
    private int HEIGHT = 144;

    public App() {
        // Set up error callback
        glfwSetErrorCallback(errorCallback = errorCallbackPrint(System.err));

        if (glfwInit() != GL11.GL_TRUE) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL11.GL_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL11.GL_FALSE);

        window = glfwCreateWindow(WIDTH, HEIGHT, "gameboylfb", MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        // It needs a window handle before it can be set
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                    glfwSetWindowShouldClose(window, GL11.GL_TRUE); // We will detect this in our rendering loop
            }
        });

        // Get the resolution of the primary monitor
        ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(
                window,
                (GLFWvidmode.width(vidmode) - WIDTH) / 2,
                (GLFWvidmode.height(vidmode) - HEIGHT) / 2
        );

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
        //GL.createCapabilities(true); // Load LWJGL's current context without deprecated functions
        GLContext.createFromCurrent(); // use this line instead with the 3.0.0a build

        // Clear buffer to Game Boy off-state display color
        GL11.glClearColor(0.605f, 0.734f, 0.059f, 1.0f);
    }

    /**
     *
     * @return Returns 0 if everything ran fine, otherwise returns 1 if the program should exit
     */
    public int tick() {
        if (glfwWindowShouldClose(window) == GL11.GL_TRUE) {
            glfwTerminate();
            errorCallback.release();
            // TODO: Notify someone that this should close in another way?
            return 1;
        } else {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
            return 0;
        }
    }
}
