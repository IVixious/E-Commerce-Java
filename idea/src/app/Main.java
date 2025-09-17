package app;

import app.admin.Admin;
import app.auth.Account;
import app.auth.AccountType;
import app.auth.AuthLog;
import app.auth.AuthManager;
import app.customer.Customer;
import app.product.Order;
import app.product.Product;
import app.product.ProductManager;
import app.product.ShoppingCart;
import app.seller.Seller;
import app.ui.ComponentHelper;
import app.ui.PlaceholderPasswordTextField;
import app.ui.PlaceholderTextField;
import app.util.ColorUtils;
import app.util.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

public class Main {
    public static final boolean IS_TESTING = false;

    private static final JFrame window = new JFrame("NVM E-Commerce System");
    public static Color topGradient = Color.WHITE;
    public static Color bottomGradient = Color.WHITE;

    public static JFrame getFrame() {
        return window;
    }

    public static void reset() {
        window.getContentPane().removeAll();
    }

    public static void refresh() {
        window.invalidate();
        window.validate();
        window.repaint();
    }

    public static void resetSizesToSmallWindow() {
        window.setPreferredSize(new Dimension(843, 600));
        window.setLocationRelativeTo(null);
        window.pack();
    }

    public static void createRegisterScreen(AccountType type, Consumer<Account> accountConsumer) {
        reset();

        topGradient = ColorUtils.fromHex(0xBA00DE);
        bottomGradient = ColorUtils.fromHex(0x7E02C7);

        var mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        var email = new PlaceholderTextField("E-mail");
        var displayName = new PlaceholderTextField("Display Name");
        var password = new PlaceholderPasswordTextField("Password");
        var confirmPassword = new PlaceholderPasswordTextField("Confirm Password");

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

            panel.add(new JLabel("You are registering a new " + type.name() + " account."));

            panel.setOpaque(false);
            mainPanel.add(panel);
        }

        {
            var panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            panel.add(
                Utils.make(email, field -> {
                    ComponentHelper.disallowWhitespace(field);
                    ComponentHelper.makePaddedAndMarginedTextField(field);
                })
            );

            panel.add(
                Utils.make(displayName, field -> {
                    ComponentHelper.makePaddedAndMarginedTextField(field);
                })
            );

            panel.add(
                Utils.make(password, field -> {
                    ComponentHelper.disallowWhitespace(field);
                    ComponentHelper.makePaddedAndMarginedTextField(field);
                })
            );

            panel.add(
                Utils.make(confirmPassword, field -> {
                    ComponentHelper.disallowWhitespace(field);
                    ComponentHelper.makePaddedAndMarginedTextField(field);
                })
            );

            panel.setOpaque(false);
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
                Utils.make(new JButton("Register"), button -> {
                    button.setAlignmentX(Component.CENTER_ALIGNMENT);

                    button.addActionListener(e -> {
                        errorText.setVisible(false);

                        try {
                            if (!password.getText().equals(confirmPassword.getText()))
                                throw new IllegalArgumentException("Passwords do not match!");

                            AuthManager authManager = getAuthManager(type);
                            var account = authManager.create(email.getText(), displayName.getText(), password.getText());

                            accountConsumer.accept(account);
                        } catch (Exception exception) {
                            errorText.setText("Error: " + exception.getMessage());
                            errorText.setVisible(true);
                            exception.printStackTrace();
                        }
                    });
                })
            );

            panel.add(Utils.make(new JPanel(new FlowLayout(FlowLayout.CENTER)), signUp -> {
                signUp.setOpaque(false);
                signUp.add(new JLabel("Already have an account? "));

                signUp.add(Utils.make(new JButton("Log in"), button -> {
                    ComponentHelper.makeHyperlink(button);
                    button.addActionListener(e -> createLoginScreen());
                }));
            }));

            mainPanel.add(panel);
        }

        mainPanel.setOpaque(false);
        window.getContentPane().add(mainPanel);

        refresh();
    }

    public static void createLoginScreen() {
        reset();

        topGradient = ColorUtils.fromHex(0x2c70f0);
        bottomGradient = ColorUtils.fromHex(0x25358e);

        var mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        var accountType = new JComboBox<>(AccountType.values());
        var email = new PlaceholderTextField("E-mail");
        var password = new PlaceholderPasswordTextField("Password");

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

            panel.add(new JLabel("Login as: "));
            panel.add(accountType);

            panel.setOpaque(false);
            mainPanel.add(panel);
        }

        {
            var panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            panel.add(
                Utils.make(email, field -> {
                    ComponentHelper.disallowWhitespace(field);
                    ComponentHelper.makePaddedAndMarginedTextField(field);
                })
            );
            panel.add(
                Utils.make(password, field -> {
                    ComponentHelper.disallowWhitespace(field);
                    ComponentHelper.makePaddedAndMarginedTextField(field);
                })
            );

            panel.setOpaque(false);
            mainPanel.add(panel);
        }

        {
            var panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panel.setOpaque(false);

            panel.add(
                Utils.make(new JCheckBox("Remember Me"), component -> {
                    component.setOpaque(false);
                    component.setForeground(Color.WHITE);
                    component.setBorder(new EmptyBorder(2, 2, 2, 2));
                })
            );

            /*panel.add(
                Utils.make(new JButton("Forgot password"), button -> {
                    ComponentHelper.makeHyperlink(button);
                })
            );*/

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
                Utils.make(new JButton("Login"), button -> {
                    button.setAlignmentX(Component.CENTER_ALIGNMENT);

                    button.addActionListener(e -> {
                        errorText.setVisible(false);

                        try {
                            AuthManager authManager = getAuthManager((AccountType) accountType.getSelectedItem());
                            var account = authManager.login(email.getText(), password.getText());

                            switch ((AccountType) Objects.requireNonNull(accountType.getSelectedItem())) {
                                case ADMINISTRATOR -> Admin.create(account);
                                case CUSTOMER -> Customer.create(account);
                                case SELLER -> Seller.create(account);
                            }
                        } catch (Exception exception) {
                            errorText.setText("Error: " + exception.getMessage());
                            errorText.setVisible(true);
                            exception.printStackTrace();
                        }
                    });
                })
            );

            panel.add(Utils.make(new JPanel(new FlowLayout(FlowLayout.CENTER)), signUp -> {
                signUp.setOpaque(false);
                signUp.add(new JLabel("Not a user? "));

                signUp.add(Utils.make(new JButton("Create an account"), button -> {
                    ComponentHelper.makeHyperlink(button);
                    button.addActionListener(e -> createRegisterScreen(AccountType.CUSTOMER, account -> {
                        Customer.create(account);
                    }));
                }));
            }));

            mainPanel.add(panel);
        }

        mainPanel.setOpaque(false);
        window.getContentPane().add(mainPanel);

        resetSizesToSmallWindow();
        refresh();
    }

    public static AuthManager getAuthManager(AccountType type) {
        AuthManager authManager;

        switch (type) {
            case ADMINISTRATOR -> authManager = Admin.getAuthManager();
            case CUSTOMER -> authManager = Customer.getAuthManager();
            case SELLER -> authManager = Seller.getAuthManager();
            case null, default -> throw new IllegalStateException("Invalid account type provided!");
        }

        return authManager;
    }

    public static void main(String[] args) {
        window.setMinimumSize(new Dimension(640, 480));
        window.setVisible(false);
        window.setResizable(false);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Sets default colours for each component
        // Key reference: https://alvinalexander.com/java/java-uimanager-color-keys-list/
        UIManager.getDefaults().put("Label.foreground", Color.WHITE);

        window.setContentPane(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                var g2d = (Graphics2D) g;
                // Makes the rendering use "quality" rendering
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                var width = this.getWidth();
                var height = this.getHeight();
                var gradient = new GradientPaint(0f, 0f, topGradient, 0f, height, bottomGradient);

                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, width, height);

                //super.paintComponent(g);
            }
        });

        // Initialize data classes, just to make sure that their serializers are registered first.
        Account.init();
        AuthLog.init();
        Order.init();
        Product.init();
        ShoppingCart.init();

        Admin.init();
        Customer.init();
        Seller.init();

        createLoginScreen();

        if (IS_TESTING) { // Add testing data
            var customer = Customer.getAuthManager().getAccountByEmail("test@test.com") == null ?
                Customer.getAuthManager().create("test@test.com", "Test Account", "testing1234")
                : Customer.getAuthManager().getAccountByEmail("test@test.com");

            var seller = Seller.getAuthManager().getAccountByEmail("test@test.com") == null ?
                Seller.getAuthManager().create("test@test.com", "Test Account", "testing1234")
                : Seller.getAuthManager().getAccountByEmail("test@test.com");

            var admin = Admin.getAuthManager().getAccountByEmail("test@test.com") == null ?
                Admin.getAuthManager().create("test@test.com", "Test Account", "testing1234")
                : Admin.getAuthManager().getAccountByEmail("test@test.com");

            var product = ProductManager.getInstance().getProduct("0123456789");
            if (product == null)
                product = ProductManager.getInstance().addProduct("0123456789", seller, "Test Product", "Description stuff yes yes", 69.42, 15);

            var product2 = ProductManager.getInstance().getProduct("01234567899");
            if (product2 == null)
                product2 = ProductManager.getInstance().addProduct("01234567899", seller, "Test Product 2", "Description stuff yes and with a long one at that too probably idk how long I can go or speak for but this is just an incredibly long description, maybe it can go longer, oh yeah also this is \"testing\" my serialization code too lmao.", 420.69, 2);

            //OrderManager.getInstance().addToCart(customer.getUUID(), product, 5);
            //OrderManager.getInstance().placeOrder(customer.getUUID());

            Admin.create(admin);
        }

        window.setVisible(true);
    }
}
