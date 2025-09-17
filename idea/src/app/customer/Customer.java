package app.customer;

import app.Main;
import app.auth.Account;
import app.auth.AccountType;
import app.auth.AuthManager;
import app.product.*;
import app.seller.Seller;
import app.ui.ComponentHelper;
import app.ui.MultilineTextLabel;
import app.ui.ScrollablePanel;
import app.ui.SharedScreens;
import app.util.ColorUtils;
import app.util.Utils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.Map;

public class Customer {
    private static final int PANEL_WIDTH = 775;
    
    private static final AuthManager authManager = new AuthManager(AccountType.CUSTOMER);

    public static AuthManager getAuthManager() {
        return authManager;
    }

    public static void init() {

    }

    public static void create(Account account) {
        Main.reset();

        var window = Main.getFrame();

        var mainPanel = new JPanel();
        mainPanel.setOpaque(false);

        var cartButton = new JButton(new ImageIcon(Utils.resizeImage("shopping_cart.png", 24, 24)));
        var updateCartLabel = (Runnable) () -> {
            cartButton.setText(OrderManager.getInstance().countCartItems(account.getUUID()) + " items");
        };

        {
            var panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setOpaque(false);

            panel.add(Utils.make(new JButton(account.getDisplayName(), new ImageIcon(Utils.getCircularImage(Utils.resizeImage("profile.png", 24, 24)))), button -> {
                button.setOpaque(false);
                button.setToolTipText("View Account Details");

                button.addActionListener(e -> {
                    SharedScreens.showAccountDetailsScreen(getAuthManager(), account, () -> create(account));
                });
            }));

            panel.add(Utils.make(cartButton, button -> {
                updateCartLabel.run();
                button.setToolTipText("View Shopping Cart");

                button.addActionListener(e -> {
                    showShoppingCartScreen(account);
                });
            }));

            panel.add(Utils.make(new JButton("Order History"), button -> {
                button.addActionListener(e -> {
                    showOrderHistoryScreen(account);
                });
            }));

            mainPanel.add(panel);
        }

        {
            var contentPanel = new ScrollablePanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setOpaque(false);

            var mainScrollPane = new JScrollPane(contentPanel);
            mainScrollPane.setBorder(new LineBorder(new Color(0f, 0f, 0f, 0.2f), 1));
            mainScrollPane.setOpaque(false);

            mainScrollPane.setPreferredSize(new Dimension(PANEL_WIDTH, 703));
            mainScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            mainScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

            for (Product product : ProductManager.getInstance().products()) {
                var panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

                panel.setPreferredSize(new Dimension(PANEL_WIDTH  - 2, 80));
                panel.setMaximumSize(new Dimension(PANEL_WIDTH  - 2, 80));
                panel.setBackground(ColorUtils.fromHex(0x0047D6));
                panel.setBorder(new LineBorder(Color.BLACK, 1, true));

                var infoPanel = new JPanel();
                infoPanel.setOpaque(false);
                infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                infoPanel.add(new JLabel(product.getName()));
                infoPanel.add(new JLabel("EAN: " + product.getBarcode()));
                infoPanel.add(new JLabel(product.getCategory().getFormatted()));
                infoPanel.add(Utils.make(new JLabel("Seller: " + Seller.getAuthManager().getAccountByUUID(product.getSeller()).getDisplayName()), label -> {
                    label.setFont(label.getFont().deriveFont(11.5f));
                }));

                panel.add(infoPanel);

                var scrollPane = new JScrollPane(Utils.make(new MultilineTextLabel(), pane -> {
                    pane.setBorder(new EmptyBorder(0, 2, 2, 2));
                    pane.setForeground(Color.WHITE);
                    pane.setText(product.getDescription());
                }));
                scrollPane.setBorder(new CompoundBorder(
                    new EmptyBorder(0, 3, 2, 3),
                    new LineBorder(new Color(0f, 0f, 0f, 0.45f), 1)
                ));
                scrollPane.getViewport().setBackground(new Color(0f, 0f, 0f, 0.2f));
                scrollPane.setOpaque(false);
                scrollPane.setPreferredSize(new Dimension(400, 50));
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

                panel.add(scrollPane);

                var numbersPanel = new JPanel();
                numbersPanel.setOpaque(false);
                numbersPanel.setLayout(new BoxLayout(numbersPanel, BoxLayout.Y_AXIS));

                var pricePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                pricePanel.setOpaque(false);
                if (product.getDiscount() > 0) {
                    pricePanel.add(new JLabel("Price: RM " + product.getPriceWithDiscount()));
                    pricePanel.add(Utils.make(new JLabel(" RM " + product.getPrice()), label -> {
                        Map attributes = label.getFont().getAttributes();
                        attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);

                        label.setFont(new Font(attributes));
                    }));
                } else {
                    pricePanel.add(new JLabel("Price: RM " + product.getPrice()));
                }

                numbersPanel.add(pricePanel);
                numbersPanel.add(Utils.make(new JLabel(product.getStock() + " in stock"), label -> {
                    if (product.getStock() <= 0) // Display red if out of stock.
                        label.setForeground(Color.RED);
                }));

                panel.add(numbersPanel);

                var spinner = new JSpinner(new SpinnerNumberModel(Math.min(1, product.getStock()), 0, product.getStock(), 1));

                panel.add(spinner);

                panel.add(Utils.make(new JButton(new ImageIcon(Utils.resizeImage("shopping_cart.png", 24, 24))), button -> {
                    button.setPreferredSize(new Dimension(24, 24));
                    button.setToolTipText("Add to Cart");

                    // If the item is out of stock, the button should be disabled.
                    button.setEnabled((int) spinner.getValue() > 0 && (int) spinner.getValue() <= product.getStock());

                    spinner.addChangeListener(e -> {
                        button.setEnabled((int) spinner.getValue() > 0 && (int) spinner.getValue() <= product.getStock());
                    });

                    button.addActionListener(e -> {
                        OrderManager.getInstance().addToCart(account.getUUID(), product, (int) spinner.getValue());
                        spinner.setValue(Math.min(1, product.getStock())); // Reset value back to 1
                        updateCartLabel.run();
                    });
                }));

                contentPanel.add(panel);
            }

            mainPanel.add(mainScrollPane);
        }

        window.getContentPane().add(mainPanel);

        window.setPreferredSize(new Dimension(1280, 768));
        window.setLocationRelativeTo(null);
        window.pack();

        Main.refresh();
    }

    public static void showShoppingCartScreen(Account account) {
        Main.reset();

        var window = Main.getFrame();

        var mainPanel = new JPanel();
        mainPanel.setOpaque(false);

        var placeOrderButton = new JButton("Place Order");

        {
            var panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setOpaque(false);

            panel.add(Utils.make(new JButton(account.getDisplayName(), new ImageIcon(Utils.getCircularImage(Utils.resizeImage("profile.png", 24, 24)))), button -> {
                button.setOpaque(false);
                button.setToolTipText("View Account Details");

                button.addActionListener(e -> {
                    SharedScreens.showAccountDetailsScreen(getAuthManager(), account, () -> create(account));
                });
            }));

            panel.add(Utils.make(new JButton("Exit"), button -> {
                button.addActionListener(e -> {
                    create(account);
                });
            }));

            panel.add(Utils.make(placeOrderButton, button -> {
                button.setEnabled(OrderManager.getInstance().countCartItems(account.getUUID()) > 0);

                button.addActionListener(e -> {
                    OrderManager.getInstance().placeOrder(account.getUUID());
                    showShoppingCartScreen(account);
                });
            }));

            panel.add(Utils.make(new JButton("Order History"), button -> {
                button.addActionListener(e -> {
                    showOrderHistoryScreen(account);
                });
            }));

            mainPanel.add(panel);
        }

        {
            var contentPanel = new ScrollablePanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setOpaque(false);

            var mainScrollPane = new JScrollPane(contentPanel);
            mainScrollPane.setBorder(new LineBorder(new Color(0f, 0f, 0f, 0.2f), 1));
            mainScrollPane.setOpaque(false);

            mainScrollPane.setPreferredSize(new Dimension(PANEL_WIDTH, 703));
            mainScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            mainScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

            var cart = OrderManager.getInstance().getCart(account.getUUID());

            if (cart.products().isEmpty()) {
                var panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                panel.setOpaque(false);

                panel.add(Utils.make(new JLabel("No items added in cart!"), label -> {
                    label.setForeground(Color.BLACK);
                }));

                contentPanel.add(panel);
            }

            cart.products().forEach((productBarcode, amount) -> {
                var product = ProductManager.getInstance().getProduct(productBarcode);
                var panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

                if (Math.min(product.getStock(), amount) <= 0)
                    return;

                panel.setPreferredSize(new Dimension(PANEL_WIDTH  - 2, 60));
                panel.setMaximumSize(new Dimension(PANEL_WIDTH  - 2, 60));
                panel.setBackground(ColorUtils.fromHex(0x0047D6));
                panel.setBorder(new LineBorder(Color.BLACK, 1, true));

                var infoPanel = new JPanel();
                infoPanel.setOpaque(false);
                infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                infoPanel.add(new JLabel(product.getName()));
                infoPanel.add(new JLabel("EAN: " + product.getBarcode()));
                infoPanel.add(Utils.make(new JLabel("Seller: " + Seller.getAuthManager().getAccountByUUID(product.getSeller()).getDisplayName()), label -> {
                    label.setFont(label.getFont().deriveFont(11.5f));
                }));

                panel.add(infoPanel);

                var scrollPane = new JScrollPane(Utils.make(new MultilineTextLabel(), pane -> {
                    pane.setBorder(new EmptyBorder(0, 2, 2, 2));
                    pane.setForeground(Color.WHITE);
                    pane.setText(product.getDescription());
                }));
                scrollPane.setBorder(new CompoundBorder(
                    new EmptyBorder(0, 3, 2, 3),
                    new LineBorder(new Color(0f, 0f, 0f, 0.45f), 1)
                ));
                scrollPane.getViewport().setBackground(new Color(0f, 0f, 0f, 0.2f));
                scrollPane.setOpaque(false);
                scrollPane.setPreferredSize(new Dimension(400, 50));
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

                panel.add(scrollPane);

                var numbersPanel = new JPanel();
                numbersPanel.setOpaque(false);
                numbersPanel.setLayout(new BoxLayout(numbersPanel, BoxLayout.Y_AXIS));

                var pricePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                pricePanel.setOpaque(false);
                if (product.getDiscount() > 0) {
                    pricePanel.add(new JLabel("Price: " + Utils.formatCurrency(product.getPriceWithDiscount())));
                    pricePanel.add(Utils.make(new JLabel(" " + Utils.formatCurrency(product.getPrice())), label -> {
                        Map attributes = label.getFont().getAttributes();
                        attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);

                        label.setFont(new Font(attributes));
                    }));
                } else {
                    pricePanel.add(new JLabel("Price: " + Utils.formatCurrency(product.getPrice())));
                }

                numbersPanel.add(pricePanel);
                numbersPanel.add(Utils.make(new JLabel(product.getStock() + " in stock"), label -> {
                    if (product.getStock() <= 0) // Display red if out of stock.
                        label.setForeground(Color.RED);
                }));

                panel.add(numbersPanel);

                var spinner = new JSpinner(new SpinnerNumberModel(Math.min(product.getStock(), amount), 1, product.getStock(), 1));
                spinner.addChangeListener(e -> {
                    cart.products().put(productBarcode, (int) spinner.getValue());
                    OrderManager.getInstance().save();
                });

                panel.add(spinner);

                panel.add(Utils.make(new JButton(new ImageIcon(Utils.getImage("trash_bin.png"))), button -> {
                    button.setPreferredSize(new Dimension(24, 24));
                    button.setToolTipText("Remove from Cart");

                    button.addActionListener(e -> {
                        cart.products().remove(productBarcode);
                        OrderManager.getInstance().save();
                        showShoppingCartScreen(account);
                    });
                }));

                contentPanel.add(panel);
            });

            mainPanel.add(mainScrollPane);
        }

        window.getContentPane().add(mainPanel);

        window.setPreferredSize(new Dimension(1280, 768));
        window.setLocationRelativeTo(null);
        window.pack();

        Main.refresh();
    }

    public static void showOrderHistoryScreen(Account account) {
        Main.reset();

        var window = Main.getFrame();

        var mainPanel = new JPanel();
        mainPanel.setOpaque(false);

        {
            var panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setOpaque(false);

            panel.add(Utils.make(new JButton(account.getDisplayName(), new ImageIcon(Utils.getCircularImage(Utils.resizeImage("profile.png", 24, 24)))), button -> {
                button.setOpaque(false);
                button.setToolTipText("View Account Details");

                button.addActionListener(e -> {
                    SharedScreens.showAccountDetailsScreen(getAuthManager(), account, () -> create(account));
                });
            }));

            panel.add(Utils.make(new JButton("Exit"), button -> {
                button.addActionListener(e -> {
                    create(account);
                });
            }));

            mainPanel.add(panel);
        }

        {
            var contentPanel = new ScrollablePanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setOpaque(false);

            var mainScrollPane = new JScrollPane(contentPanel);
            mainScrollPane.setBorder(new LineBorder(new Color(0f, 0f, 0f, 0.2f), 1));
            mainScrollPane.setOpaque(false);

            mainScrollPane.setPreferredSize(new Dimension(PANEL_WIDTH, 703));
            mainScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            mainScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

            var orders = OrderManager.getInstance().getOrderHistory(account.getUUID());

            if (orders.isEmpty()) {
                var panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                panel.setOpaque(false);

                panel.add(Utils.make(new JLabel("No prior order history found!"), label -> {
                    label.setForeground(Color.BLACK);
                }));

                contentPanel.add(panel);
            }

            for (Order order : orders.reversed()) {
                var panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

                panel.setPreferredSize(new Dimension(PANEL_WIDTH  - 2, 80));
                panel.setMaximumSize(new Dimension(PANEL_WIDTH  - 2, 80));
                panel.setBackground(ColorUtils.fromHex(0x0047D6));
                panel.setBorder(new LineBorder(Color.BLACK, 1, true));

                var infoPanel = new JPanel();
                infoPanel.setOpaque(false);
                infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                infoPanel.add(new JLabel("Order ID: #" + order.getOrderId()));
                infoPanel.add(new JLabel("Ordered at " + Utils.getDateTimeString(order.getOrderTimestamp())));

                if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
                    infoPanel.add(Utils.make(new JButton("Request Refund"), button -> {
                        ComponentHelper.makeHyperlink(button);
                        button.addActionListener(e -> {
                            order.setPaymentStatus(PaymentStatus.REQUESTING_REFUND);
                            OrderManager.getInstance().save();
                            showOrderHistoryScreen(account);
                        });
                    }));
                } else if (order.getPaymentStatus() == PaymentStatus.PENDING) {
                    infoPanel.add(Utils.make(new JButton("Make Payment"), button -> {
                        ComponentHelper.makeHyperlink(button);
                        button.addActionListener(e -> {
                            order.setPaymentStatus(PaymentStatus.COMPLETED);
                            OrderManager.getInstance().save();
                            showOrderHistoryScreen(account);
                        });
                    }));
                }

                panel.add(infoPanel);

                var scrollPane = new JScrollPane(Utils.make(new ScrollablePanel(), pane -> {
                    pane.setBorder(new EmptyBorder(0, 2, 2, 2));
                    pane.setForeground(Color.WHITE);
                    pane.setOpaque(false);

                    order.getProducts().forEach((barcode, amount) -> {
                        var product = ProductManager.getInstance().getProduct(barcode);
                        pane.add(new JLabel(amount + "x - " + product.getName()));
                        pane.add(new JLabel(" -  Seller: " + Seller.getAuthManager().getAccountByUUID(product.getSeller()).getDisplayName()));
                    });
                }));
                scrollPane.setBorder(new CompoundBorder(
                    new EmptyBorder(0, 3, 2, 3),
                    new LineBorder(new Color(0f, 0f, 0f, 0.45f), 1)
                ));
                scrollPane.getViewport().setBackground(new Color(0f, 0f, 0f, 0.2f));
                scrollPane.setOpaque(false);
                scrollPane.setPreferredSize(new Dimension(400, 70));
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

                panel.add(scrollPane);

                var numbersPanel = new JPanel();
                numbersPanel.setOpaque(false);
                numbersPanel.setLayout(new BoxLayout(numbersPanel, BoxLayout.Y_AXIS));

                numbersPanel.add(new JLabel(Utils.formatCurrency(order.getTotalCost())));
                numbersPanel.add(new JLabel("Status: " + order.getStatus().getFormatted()));
                numbersPanel.add(new JLabel("Payment: " + order.getPaymentStatus().getFormatted()));

                panel.add(numbersPanel);

                contentPanel.add(panel);
            }

            mainPanel.add(mainScrollPane);
        }

        window.getContentPane().add(mainPanel);

        window.setPreferredSize(new Dimension(1280, 768));
        window.setLocationRelativeTo(null);
        window.pack();

        Main.refresh();
    }
}
