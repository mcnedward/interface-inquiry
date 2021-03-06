package com.mcnedward.app;

import com.mcnedward.app.ui.form.MainPage;
import com.mcnedward.app.utils.*;
import com.mcnedward.ii.utils.IILogger;

import javax.swing.*;
import java.awt.*;

/**
 * @author Edward - Aug 28, 2016
 *
 */
public class InheritanceInquiry {

    public static JFrame PARENT_FRAME;
    public static final String FONT_NAME = "Segoe UI";
    public static int WIDTH;
    public static int HEIGHT;

    public static void main(String[] args) {
        IILogger.setLogLevels(false, false, false, true);
        EventQueue.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("Inheritance Inquiry");
                PARENT_FRAME = frame;
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                IIAppUtils.loadIcon(frame);

                try {
                    UIManager.put("ScrollBarUI", "com.mcnedward.app.ui.component.IIScrollBar");
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    loadTheme();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                    IILogger.error("Something went wrong when trying to use the System Look and Feel...", e);
                }

                boolean useFullScreen = PrefUtils.getPreferenceBool(Constants.FULL_SCREEN, InheritanceInquiry.class);

                GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                int screenWidth = gd.getDisplayMode().getWidth();
                int screenHeight = gd.getDisplayMode().getHeight();
                int x, y;
                if (useFullScreen) {
                    x = 0;
                    y = 0;
                    WIDTH = screenWidth;
                    HEIGHT = screenHeight;
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                } else {
                    x = 100;
                    y = 100;
                    WIDTH = (int) (screenWidth * 0.8f);
                    HEIGHT = (int) (screenHeight * 0.8f);
                    frame.setBounds(x, y, WIDTH, HEIGHT);
                    frame.setLocation(screenWidth / 2 - WIDTH / 2, screenHeight / 2 - HEIGHT / 2);
                }

                int minWidth = (int) (screenWidth * 0.6f);
                int minHeight = (int) (screenHeight * 0.6f);
                frame.setMinimumSize(new Dimension(minWidth, minHeight));
                frame.setMaximumSize(new Dimension(WIDTH, HEIGHT));

                PrefUtils.loadGraphDefaults();

                MainPage mainPage = new MainPage();
                frame.setContentPane(mainPage.getRoot());

                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void loadTheme() {
        Theme currentTheme = Theme.getCurrentTheme();
        if (currentTheme.equals(Theme.STANDARD)) return; // Don't load if default
        Theme.setTheme(currentTheme);
    }
}
