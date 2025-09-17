package app.ui;

import app.Main;
import app.auth.Account;
import app.auth.AuthManager;
import app.util.ColorUtils;
import app.util.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;

public class SharedScreens {
    // Account Details screen
    public static void showAccountDetailsScreen(AuthManager authManager, Account account, Runnable onExit) {
        showAccountDetailsScreen(authManager, account, account, onExit);
    }

    public static void showAccountDetailsScreen(AuthManager authManager, Account account, Account accountToNavigate, Runnable onExit) {
        Main.reset();

        var window = Main.getFrame();

        var mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);

        {
            var panel = new JPanel();
            panel.setOpaque(false);
            // not sure why Swing's not allowing us to resize anything, so we're just adding padding ourselves.
            panel.add(new JLabel(new ImageIcon(Utils.createEmptyImage(15, 18))));
            mainPanel.add(panel);
        }

        {
            var panel = new JPanel();
            panel.setOpaque(false);

            try {
                var image = Utils.getCircularImage(ImageIO.read(Main.class.getResourceAsStream("/images/profile.png")))
                    .getScaledInstance(128, 128, Image.SCALE_SMOOTH);
                panel.add(new JLabel(new ImageIcon(image)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            mainPanel.add(panel);
        }

        var email = new PlaceholderTextField("E-mail");
        var displayName = new PlaceholderTextField("Display Name");

        {
            var panel = new JPanel();
            panel.setOpaque(false);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            panel.add(new JLabel("Email"));
            panel.add(
                Utils.make(email, field -> {
                    field.setText(account.getEmail());
                    ComponentHelper.disallowWhitespace(field);
                    ComponentHelper.makePaddedAndMarginedTextField(field);
                })
            );

            panel.add(new JLabel("Display Name"));
            panel.add(
                Utils.make(displayName, field -> {
                    field.setText(account.getDisplayName());
                    ComponentHelper.makePaddedAndMarginedTextField(field);
                })
            );

            mainPanel.add(panel);
        }

        {
            var panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setOpaque(false);

            if (account == accountToNavigate) {
                panel.add(Utils.make(new JPanel(new FlowLayout(FlowLayout.CENTER)), changePassword -> {
                    changePassword.setOpaque(false);
                    changePassword.add(Utils.make(new JButton("Change Password"), button -> {
                        ComponentHelper.makeHyperlink(button);
                        button.addActionListener(e -> showChangePasswordScreen(authManager, account, onExit));
                    }));
                }));
            }

            panel.add(
                Utils.make(new JButton("Save"), button -> {
                    button.setAlignmentX(Component.CENTER_ALIGNMENT);
                    button.setEnabled(false);

                    button.addActionListener(e -> {
                        account.setEmail(email.getText());
                        account.setDisplayName(displayName.getText());

                        authManager.save();
                    });

                    var changeEvent = (ActionListener) e -> {
                        // Disable button if the email or display name is blank - we do not allow empty emails or display names.
                        if (email.getText().isBlank() || displayName.getText().isBlank())
                            button.setEnabled(false);
                        else if (email.getText().equals(account.getEmail()) && displayName.getText().equals(account.getDisplayName()))
                            button.setEnabled(false); // If they're the same, don't enable the Save button
                        else
                            button.setEnabled(true); // Otherwise just allow the button to be enabled
                    };

                    // Add the shared action event to both email and display name
                    email.addActionListener(changeEvent);
                    displayName.addActionListener(changeEvent);
                })
            );

            panel.add(
                Utils.make(new JButton("Exit"), button -> {
                    button.setAlignmentX(Component.CENTER_ALIGNMENT);

                    button.addActionListener(e -> {
                        onExit.run();
                    });
                })
            );

            if (accountToNavigate == account) {
                panel.add(
                    Utils.make(new JButton("Log Out"), button -> {
                        button.setAlignmentX(Component.CENTER_ALIGNMENT);

                        button.addActionListener(e -> {
                            Main.createLoginScreen();
                        });
                    })
                );
            }

            mainPanel.add(panel);
        }

        window.getContentPane().add(mainPanel);

        window.setPreferredSize(new Dimension(1280, 768));
        window.setLocationRelativeTo(null);
        window.pack();

        Main.refresh();
    }

    // Change Password screen
    public static void showChangePasswordScreen(AuthManager authManager, Account account, Runnable onExit) {
        Main.reset();

        var window = Main.getFrame();

        var mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);

        var oldPassword = new PlaceholderPasswordTextField("Old Password");
        var newPassword = new PlaceholderPasswordTextField("New Password");
        var confirmNewPassword = new PlaceholderPasswordTextField("Confirm New Password");

        {
            var panel = new JPanel();
            panel.setOpaque(false);
            // not sure why Swing's not allowing us to resize anything, so we're just adding padding ourselves.
            panel.add(new JLabel(new ImageIcon(Utils.createEmptyImage(15, 18))));
            mainPanel.add(panel);
        }

        {
            var panel = new JPanel();
            panel.setOpaque(false);

            try {
                var image = Utils.getCircularImage(ImageIO.read(Main.class.getResourceAsStream("/images/profile.png")))
                    .getScaledInstance(128, 128, Image.SCALE_SMOOTH);
                panel.add(new JLabel(new ImageIcon(image)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            mainPanel.add(panel);
        }

        {
            var panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

            panel.add(new JLabel("Changing password for " + account.getDisplayName()));

            panel.setOpaque(false);
            mainPanel.add(panel);
        }

        {
            var panel = new JPanel();
            panel.setOpaque(false);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            panel.add(
                Utils.make(oldPassword, field -> {
                    ComponentHelper.disallowWhitespace(field);
                    ComponentHelper.makePaddedAndMarginedTextField(field);
                })
            );

            panel.add(
                Utils.make(newPassword, field -> {
                    ComponentHelper.disallowWhitespace(field);
                    ComponentHelper.makePaddedAndMarginedTextField(field);
                })
            );

            panel.add(
                Utils.make(confirmNewPassword, field -> {
                    ComponentHelper.disallowWhitespace(field);
                    ComponentHelper.makePaddedAndMarginedTextField(field);
                })
            );

            mainPanel.add(panel);
        }

        var errorText = new JLabel("Error: [unknown]");
        errorText.setForeground(ColorUtils.fromHex(0xFF5A5A));
        errorText.setFont(errorText.getFont().deriveFont(14f));

        {
            var panel = new JPanel();
            panel.setOpaque(false);
            panel.setLayout(new FlowLayout(FlowLayout.CENTER));
            errorText.setVisible(false);
            panel.add(errorText);
            mainPanel.add(panel);
        }

        {
            var panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setOpaque(false);

            panel.add(
                Utils.make(new JButton("Save"), button -> {
                    button.setAlignmentX(Component.CENTER_ALIGNMENT);
                    button.setEnabled(false);

                    button.addActionListener(e -> {
                        try {
                            authManager.changePassword(account, oldPassword.getText(), newPassword.getText());
                        } catch (Exception exception) {
                            errorText.setText("Error: " + exception.getMessage());
                            errorText.setVisible(true);
                            exception.printStackTrace();
                        }

                        showAccountDetailsScreen(authManager, account, onExit);
                    });

                    var changeEvent = (ActionListener) e -> {
                        // Disable button if the email or display name is blank - we do not allow empty emails or display names.
                        if (oldPassword.getText().isBlank() || newPassword.getText().isBlank() || confirmNewPassword.getText().isBlank())
                            button.setEnabled(false);
                        else
                            button.setEnabled(newPassword.getText().equals(confirmNewPassword.getText())); // If they're the same, enable the Save button
                    };

                    // Add the shared action event to all password fields
                    oldPassword.addActionListener(changeEvent);
                    newPassword.addActionListener(changeEvent);
                    confirmNewPassword.addActionListener(changeEvent);
                })
            );

            panel.add(
                Utils.make(new JButton("Exit"), button -> {
                    button.setAlignmentX(Component.CENTER_ALIGNMENT);

                    button.addActionListener(e -> {
                        showAccountDetailsScreen(authManager, account, onExit);
                    });
                })
            );

            mainPanel.add(panel);
        }

        window.getContentPane().add(mainPanel);

        window.setPreferredSize(new Dimension(1280, 768));
        window.setLocationRelativeTo(null);
        window.pack();

        Main.refresh();
    }
}
