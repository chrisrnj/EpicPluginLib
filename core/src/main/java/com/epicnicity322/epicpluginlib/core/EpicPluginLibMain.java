/*
 * EpicPluginLib - Library with basic utilities for Minecraft plugins.
 * Copyright (C) 2026 Christiano Rangel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.epicnicity322.epicpluginlib.core;

import javax.swing.*;

public final class EpicPluginLibMain
{
    private EpicPluginLibMain()
    {
    }

    public static void main(String[] args)
    {
        System.out.println("I am a library, not an application!");

        JFrame frame = new JFrame("Sorry");
        JLabel textArea = new JLabel(" I am a library, not an application!");

        frame.add(textArea);
        frame.setSize(210, 60);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        try {
            Thread.sleep(7500);
        } catch (Exception ignored) {
        }

        System.exit(0);
    }
}
