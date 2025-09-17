package app.seller;

import app.Main;
import app.auth.Account;
import app.customer.Customer;
import app.product.*;
import app.ui.*;
import app.util.ColorUtils;
import app.util.Utils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class SellerViews {
    private static final int PANEL_WIDTH = 775;

    public static void createManageOrdersScreen(Account account) {
        Main.reset();

        var window = Main.getFrame();

        window.setPreferredSize(new Dimension(1280, 768));
        window.setLocationRelativeTo(null);
        window.pack();

        var mainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mainPanel.setOpaque(false);
        mainPanel.add(SellerViews.createSidebar(account));

        var secondPanel = new JPanel();
        secondPanel.setOpaque(false);

        {
            var contentPanel = new ScrollablePanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setOpaque(false);

            var mainScrollPane = new JScrollPane(contentPanel);
            mainScrollPane.setBorder(new LineBorder(new Color(0f, 0f, 0f, 0.2f), 1));
            mainScrollPane.setOpaque(false);

            mainScrollPane.setPreferredSize(new Dimension(PANEL_WIDTH, 650));
            mainScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            mainScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

            var orders = OrderManager.getInstance().getAllOrdersWithSeller(account.getUUID());

            if (orders.isEmpty()) {
                var panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                panel.setOpaque(false);

                panel.add(Utils.make(new JLabel("No orders have been made!"), label -> {
                    label.setForeground(Color.BLACK);
                }));

                contentPanel.add(panel);
            }

            for (Order order : orders.reversed()) {
                var products = OrderManager.getInstance().getAllProductsForSeller(account.getUUID(), order);
                var panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

                panel.setPreferredSize(new Dimension(PANEL_WIDTH  - 2, 120));
                panel.setMaximumSize(new Dimension(PANEL_WIDTH  - 2, 120));
                panel.setBackground(ColorUtils.fromHex(0x0047D6));
                panel.setBorder(new LineBorder(Color.BLACK, 1, true));

                var infoPanel = new JPanel();
                infoPanel.setOpaque(false);
                infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                infoPanel.add(new JLabel("Order #" + order.getOrderId()));
                infoPanel.add(Utils.make(new JLabel("Customer: " + Customer.getAuthManager().getAccountByUUID(order.getAccountUUID()).getDisplayName()), label -> {
                    label.setFont(label.getFont().deriveFont(11.5f));
                }));

                panel.add(infoPanel);

                var scrollPane = new JScrollPane(Utils.make(new ScrollablePanel(), pane -> {
                    pane.setBorder(new EmptyBorder(0, 2, 2, 2));
                    pane.setForeground(Color.WHITE);
                    pane.setOpaque(false);

                    products.forEach((barcode, amount) -> {
                        pane.add(new JLabel(amount + "x - " + ProductManager.getInstance().getProduct(barcode).getName() + "(EAN: " + barcode + ")"));
                    });
                }));
                scrollPane.setBorder(new CompoundBorder(
                    new EmptyBorder(0, 3, 2, 3),
                    new LineBorder(new Color(0f, 0f, 0f, 0.45f), 1)
                ));
                scrollPane.getViewport().setBackground(new Color(0f, 0f, 0f, 0.2f));
                scrollPane.setOpaque(false);
                scrollPane.setPreferredSize(new Dimension(400, 110));
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

                panel.add(scrollPane);

                var numbersPanel = new JPanel();
                numbersPanel.setOpaque(false);
                numbersPanel.setLayout(new BoxLayout(numbersPanel, BoxLayout.Y_AXIS));

                numbersPanel.add(new JLabel("Status: " + order.getStatus().getFormatted()));
                numbersPanel.add(new JLabel("Payment: " + order.getPaymentStatus().getFormatted()));

                if (order.getStatus() == OrderStatus.PENDING && order.getPaymentStatus() == PaymentStatus.COMPLETED) {
                    numbersPanel.add(Utils.make(new JButton("Mark for Delivery"), button -> {
                        button.addActionListener(e -> {
                            order.setStatus(OrderStatus.READY_FOR_DELIVERY);
                            OrderManager.getInstance().save();
                            createManageOrdersScreen(account);
                        });
                    }));
                } else if (order.getPaymentStatus() == PaymentStatus.REQUESTING_REFUND && order.getStatus() != OrderStatus.DELIVERED) {
                    numbersPanel.add(Utils.make(new JButton("Refund Order"), button -> {
                        button.addActionListener(e -> {
                            order.setStatus(OrderStatus.CANCELLED);
                            order.setPaymentStatus(PaymentStatus.REFUNDED);
                            OrderManager.getInstance().save();
                            createManageOrdersScreen(account);
                        });
                    }));

                    numbersPanel.add(Utils.make(new JButton("Deny Refund"), button -> {
                        button.addActionListener(e -> {
                            order.setPaymentStatus(PaymentStatus.COMPLETED);
                            OrderManager.getInstance().save();
                            createManageOrdersScreen(account);
                        });
                    }));
                }

                if (order.getStatus() != OrderStatus.CANCELLED && order.getStatus() != OrderStatus.DELIVERED) {
                    numbersPanel.add(Utils.make(new JButton("Cancel Order"), button -> {
                        button.addActionListener(e -> {
                            order.setStatus(OrderStatus.CANCELLED);
                            order.setPaymentStatus(PaymentStatus.REFUNDED);
                            OrderManager.getInstance().save();
                            createManageOrdersScreen(account);
                        });
                    }));
                }

                panel.add(numbersPanel);

                contentPanel.add(panel);
            }

            secondPanel.add(mainScrollPane);
        }

        mainPanel.add(secondPanel);
        window.getContentPane().add(mainPanel);

        Main.refresh();
    }

    public static void createManageProductsScreen(Account account) {
        Main.reset();

        var window = Main.getFrame();

        window.setPreferredSize(new Dimension(1280, 768));
        window.setLocationRelativeTo(null);
        window.pack();

        var mainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mainPanel.setOpaque(false);
        mainPanel.add(Utils.make(SellerViews.createSidebar(account), sidebar -> {
            sidebar.add(Utils.make(new JButton("Add Product"), button -> {
                button.addActionListener(e -> {
                    createAddProductScreen(account);
                });
            }));
        }));

        var secondPanel = new JPanel();
        secondPanel.setOpaque(false);

        {
            var contentPanel = new ScrollablePanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setOpaque(false);

            var mainScrollPane = new JScrollPane(contentPanel);
            mainScrollPane.setBorder(new LineBorder(new Color(0f, 0f, 0f, 0.2f), 1));
            mainScrollPane.setOpaque(false);

            mainScrollPane.setPreferredSize(new Dimension(PANEL_WIDTH, 650));
            mainScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            mainScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

            var products = ProductManager.getInstance().products();

            if (products.isEmpty()) {
                var panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                panel.setOpaque(false);

                panel.add(Utils.make(new JLabel("You do not have any products!"), label -> {
                    label.setForeground(Color.BLACK);
                }));

                contentPanel.add(panel);
            }

            for (Product product : ProductManager.getInstance().products()) {
                var panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

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

                panel.add(Utils.make(new JButton(new ImageIcon(Utils.resizeImage("pencil.png", 24, 24))), button -> {
                    button.setPreferredSize(new Dimension(24, 24));
                    button.setToolTipText("Edit");

                    button.addActionListener(e -> {
                        createEditProductScreen(account, product);
                    });
                }));

                panel.add(Utils.make(new JButton(new ImageIcon(Utils.resizeImage("trash_bin.png", 24, 24))), button -> {
                    button.setPreferredSize(new Dimension(24, 24));
                    button.setToolTipText("Remove");

                    button.addActionListener(e -> {
                        ProductManager.getInstance().removeProduct(product.getBarcode());
                        createManageProductsScreen(account);
                    });
                }));

                contentPanel.add(panel);
            }

            secondPanel.add(mainScrollPane);
        }

        mainPanel.add(secondPanel);
        window.getContentPane().add(mainPanel);

        Main.refresh();
    }

    public static void createEditProductScreen(Account account, Product product) {
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

        var name = new PlaceholderTextField("Name");
        var description = new ListenableTextArea();
        var price = new JSpinner(new SpinnerNumberModel(product.getPrice(), 0.01, 100_000.0, 1.0));
        var stock = new JSpinner(new SpinnerNumberModel(product.getStock(), 0, 1_000_000_000, 1));

        {
            var panel = new JPanel();
            panel.setOpaque(false);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            panel.add(Utils.make(new JLabel("Name"), label -> {
                label.setAlignmentX(0.5f);
            }));
            panel.add(
                Utils.make(name, field -> {
                    field.setText(product.getName());
                    ComponentHelper.makePaddedAndMarginedTextField(field);
                })
            );

            panel.add(Utils.make(new JLabel("Description"), label -> {
                label.setAlignmentX(0.5f);
            }));
            panel.add(
                Utils.make(description, field -> {
                    field.setLineWrap(true);
                    field.setWrapStyleWord(true);
                    field.setPreferredSize(new Dimension(350, 230));
                    field.setText(product.getDescription());
                })
            );

            panel.add(Utils.make(new JLabel("Price"), label -> {
                label.setAlignmentX(0.5f);
            }));
            panel.add(price);

            panel.add(Utils.make(new JLabel("Stock"), label -> {
                label.setAlignmentX(0.5f);
            }));
            panel.add(stock);

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
                        product.setName(name.getText());
                        product.setDescription(description.getText());
                        product.setPrice((double) price.getValue());
                        product.setStock((int) stock.getValue());

                        ProductManager.getInstance().save();
                        createManageProductsScreen(account);
                    });

                    var changeEvent = (ActionListener) e -> {
                        // Disable button if either is blank - we do not allow empty values.
                        if (name.getText().isBlank() || description.getText().isBlank())
                            button.setEnabled(false);
                        else if (name.getText().equals(product.getName()) && description.getText().equals(product.getDescription()) && (double) price.getValue() == product.getPrice() && (int) stock.getValue() == product.getStock())
                            button.setEnabled(false); // If they're the same, don't enable the Save button
                        else
                            button.setEnabled(true); // Otherwise just allow the button to be enabled
                    };

                    // Add the shared action event to both
                    name.addActionListener(changeEvent);
                    description.addActionListener(changeEvent);
                    price.addChangeListener(e -> changeEvent.actionPerformed(null));
                    stock.addChangeListener(e -> changeEvent.actionPerformed(null));
                })
            );

            panel.add(
                Utils.make(new JButton("Exit"), button -> {
                    button.setAlignmentX(Component.CENTER_ALIGNMENT);

                    button.addActionListener(e -> {
                        createManageProductsScreen(account);
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

    public static void createAddProductScreen(Account account) {
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

        var barcode = new PlaceholderTextField("Barcode");
        var name = new PlaceholderTextField("Name");
        var description = new ListenableTextArea();
        var price = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 100_000.0, 1.0));
        var stock = new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000_000, 1));

        {
            var panel = new JPanel();
            panel.setOpaque(false);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            panel.add(Utils.make(new JLabel("Barcode"), label -> {
                label.setAlignmentX(0.5f);
            }));
            panel.add(
                Utils.make(barcode, field -> {
                    ComponentHelper.disallowWhitespace(field);
                    ComponentHelper.makePaddedAndMarginedTextField(field);
                })
            );

            panel.add(Utils.make(new JLabel("Name"), label -> {
                label.setAlignmentX(0.5f);
            }));
            panel.add(
                Utils.make(name, field -> {
                    ComponentHelper.makePaddedAndMarginedTextField(field);
                })
            );

            panel.add(Utils.make(new JLabel("Description"), label -> {
                label.setAlignmentX(0.5f);
            }));
            panel.add(
                Utils.make(description, field -> {
                    field.setLineWrap(true);
                    field.setWrapStyleWord(true);
                    field.setPreferredSize(new Dimension(350, 230));
                })
            );

            panel.add(Utils.make(new JLabel("Price"), label -> {
                label.setAlignmentX(0.5f);
            }));
            panel.add(price);

            panel.add(Utils.make(new JLabel("Stock"), label -> {
                label.setAlignmentX(0.5f);
            }));
            panel.add(stock);

            mainPanel.add(panel);
        }

        {
            var panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setOpaque(false);

            panel.add(
                Utils.make(new JButton("Save"), button -> {
                    button.setAlignmentX(Component.CENTER_ALIGNMENT);

                    button.addActionListener(e -> {
                        ProductManager.getInstance().addProduct(barcode.getText(), account, name.getText(), description.getText(), (double) price.getValue(), (int) stock.getValue());

                        createManageProductsScreen(account);
                    });
                })
            );

            panel.add(
                Utils.make(new JButton("Exit"), button -> {
                    button.setAlignmentX(Component.CENTER_ALIGNMENT);

                    button.addActionListener(e -> {
                        createManageProductsScreen(account);
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

    public static JPanel createSidebar(Account account) {
        var sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(192, Main.getFrame().getHeight()));
        sidebar.setAlignmentX(0f);
        sidebar.setLocation(0, 0);
        sidebar.setOpaque(false);
        //sidebar.setBackground(ColorUtils.fromHex(0x2F70D8));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // Sellers panel
        {
            sidebar.add(makeDropdownPanel("Seller", List.of(
                new DropdownItem("Home", () -> Seller.create(account)),
                new DropdownItem("Manage Orders", () -> createManageOrdersScreen(account)),
                new DropdownItem("Manage Products", () -> createManageProductsScreen(account))
            )));
        }

        sidebar.add(Utils.make(new JButton(account.getDisplayName(), new ImageIcon(Utils.getCircularImage(Utils.resizeImage("profile.png", 24, 24)))), button -> {
            button.setOpaque(false);
            button.setToolTipText("View Account Details");

            button.addActionListener(e -> {
                SharedScreens.showAccountDetailsScreen(Seller.getAuthManager(), account, () -> Seller.create(account));
            });
        }));

        return sidebar;
    }

    private record DropdownItem(String name, Runnable clickHandler) {}

    private static JPanel makeDropdownPanel(String name, List<DropdownItem> dropdownItems) {
        var panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        var isOpen = new AtomicBoolean(true);

        var subcontainer = new JPanel();
        subcontainer.setBorder(new EmptyBorder(2, 5, 5, 5));
        subcontainer.setLayout(new BoxLayout(subcontainer, BoxLayout.Y_AXIS));
        subcontainer.setOpaque(false);

        // ▲▼
        var button = new JButton(name + " ▲");
        button.setOpaque(false);
        button.setFont(button.getFont().deriveFont(16f));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(2, 2, 2, 2));
        button.setBackground(ColorUtils.NONE);
        button.addActionListener(e -> {
            if (isOpen.get()) {
                button.setText(name + " ▼");
                subcontainer.setVisible(false);
                isOpen.set(false);
            } else {
                button.setText(name + " ▲");
                subcontainer.setVisible(true);
                isOpen.set(true);
            }
        });

        panel.add(button);

        for (DropdownItem item : dropdownItems) {
            subcontainer.add(Utils.make(new JButton(item.name()), itemBtn -> {
                ComponentHelper.makeHyperlink(itemBtn);
                itemBtn.addActionListener(e -> item.clickHandler().run());
            }));
        }

        panel.add(subcontainer);
        return panel;
    }
}
