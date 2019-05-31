/*
 *
 *    Copyright 2019 Lawrence Kesteloot
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

import com.teamten.image.ImageUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class Breville {
    private static final int DPI = 300;
    private static final int WIDTH = 11*DPI;
    private static final int HEIGHT = 14*DPI;
    private static final int BUTTON_X = WIDTH/2;
    private static final int BUTTON_Y = HEIGHT/2;
    private static final int BUTTON_RADIUS = WIDTH*35/100;
    private static final int LIGHT_RADIUS = WIDTH*40/100;
    // Vector to light source. Must be normalized.
    private static final double LIGHT_DX = -1/Math.sqrt(2);
    private static final double LIGHT_DY = 1/Math.sqrt(2);
    private static final double[] RANDOM_VALUES = new double[10*1024];

    static {
        for (int i = 0; i < RANDOM_VALUES.length; i++) {
            RANDOM_VALUES[i] = Math.random();
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedImage image = ImageUtils.makeWhite(WIDTH, HEIGHT);
        BufferedImage glow = ImageUtils.makeTransparent(WIDTH, HEIGHT);

        for (int y = 0; y < HEIGHT; y++) {
            int cy = y - BUTTON_Y;
            for (int x = 0; x < WIDTH; x++) {
                int cx = x - BUTTON_X;

                double dx;
                double dy;
                int line;
                int color;
                boolean addToGlow = false;

                double r = Math.sqrt(cx*cx + cy*cy);
                if (r <= LIGHT_RADIUS) {
                    dx = cx/r;
                    dy = cy/r;
                    line = (int) r;
                    if (r <= BUTTON_RADIUS) {
                        color = 0xFFFFFF;
                    } else {
                        color = 0xA02020;
                        addToGlow = true;
                    }
                } else {
                    dx = 0;
                    dy = 1;
                    line = y;
                    color = 0xFFFFFF;
                }

                drawPixel(image, x, y, dx, dy, RANDOM_VALUES[line], color);

                if (addToGlow) {
                    drawPixel(glow, x, y, dx, dy, RANDOM_VALUES[line], color);
//                    glow.setRGB(x, y, 0xFFFFFFFF);
                }
            }
        }

//        ImageUtils.save(glow, "before.png");
//        ImageUtils.save(ImageUtils.blur(glow, WIDTH*3/100), "after.png");

        glow = ImageUtils.glow(glow, 5, WIDTH*3/100);
        image = ImageUtils.compose(image, glow);

        ImageUtils.save(image, "breville.png");
    }

    /**
     * dx and dy are perpendicular to the direction of the ridge, and are normalized to
     * a length of 1.
     */
    private static void drawPixel(BufferedImage image, int x, int y, double dx, double dy,
                                  double variance, int color) {

        double light = Math.abs(dx*LIGHT_DX + dy*LIGHT_DY);

        double ambient = 0.7;
        light = ambient + Math.pow(light, 10)*(1 - ambient);

        // Add some randomness.
        light = light*(0.9 + 0.1*variance);

        image.setRGB(x, y, multiplyColor(color, light));
    }

    private static int multiplyColor(int color, double m) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = (color >> 0) & 0xFF;

        red = (int) (red*m + 0.5);
        green = (int) (green*m + 0.5);
        blue = (int) (blue*m + 0.5);

        return (255 << 24) | (red << 16) | (green << 8) | (blue << 0);
    }
}
