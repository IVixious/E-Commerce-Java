package app.admin;

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
import java.awt.event.ItemEvent;
import java.awt.font.TextAttribute;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class AdminViews {
    private static final int PANEL_WIDTH = 775;

    public static void createHomeScreen(Account account) {
        Main.reset();

        var window = Main.getFrame();
        window.setPreferredSize(new Dimension(1280, 768));
        window.setLocationRelativeTo(null);
        window.pack();

        var mainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mainPanel.setOpaque(false);
        mainPanel.add(createSidebar(account));

        var secondPanel = new JPanel();
        secondPanel.setOpaque(false);

        {
            var formatter = DateTimeFormatter.ofPattern("MMM uuuu", Locale.US);
            var monthlyOrders = new HashMap<YearMonth, List<Order>>(); // (Month, Year) -> [orders]

            for (Order order : OrderManager.getInstance().getAllOrders()) {
                // Ignore orders with pending or failed payment statuses
                if (order.getPaymentStatus() == PaymentStatus.PENDING || order.getPaymentStatus() == PaymentStatus.FAILED)
                    continue;

                var yearMonth = YearMonth.from(
                    Instant.ofEpochMilli(order.getOrderTimestamp())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                );
                var ordersList = monthlyOrders.computeIfAbsent(yearMonth, $ -> new ArrayList<>());
                ordersList.add(order);
            }

            var sortedMonths = monthlyOrders.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .toList();

            var monthsList = new JComboBox<>(sortedMonths.stream().map(formatter::format).toArray());
            secondPanel.add(monthsList);

            var statsPanel = new JPanel();
            statsPanel.setOpaque(false);

            var createStatsPanel = (Runnable) () -> {
                statsPanel.removeAll();

                var grossEarnings = new AtomicReference<Double>();

                var scrollableList = new JScrollPane(Utils.make(new ScrollablePanel(), scrollable -> {
                    scrollable.setLayout(new BoxLayout(scrollable, BoxLayout.Y_AXIS));

                    var yearMonth = sortedMonths.get(monthsList.getSelectedIndex());
                    var orders = monthlyOrders.get(yearMonth);

                    scrollable.setOpaque(false);

                    var grossEarningsDouble = 0.0;

                    for (Order order : orders.reversed()) {
                        var panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

                        panel.setPreferredSize(new Dimension(PANEL_WIDTH  - 2, 60));
                        panel.setMaximumSize(new Dimension(PANEL_WIDTH  - 2, 60));
                        panel.setBackground(ColorUtils.fromHex(0x0047D6));
                        panel.setBorder(new LineBorder(Color.BLACK, 1, true));

                        var infoPanel = new JPanel();
                        infoPanel.setOpaque(false);
                        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                        infoPanel.add(new JLabel("Order #" + order.getOrderId()));
                        infoPanel.add(new JLabel("Total Earned: " + Utils.formatCurrency(order.getTotalCost())));
                        infoPanel.add(new JLabel("Ordered at " + Utils.getDateTimeString(order.getOrderTimestamp())));

                        grossEarningsDouble += order.getTotalCost();

                        panel.add(infoPanel);

                        var scrollPane = new JScrollPane(Utils.make(new ScrollablePanel(), pane -> {
                            pane.setOpaque(false);
                            pane.setBorder(new EmptyBorder(0, 2, 2, 2));
                            pane.setForeground(Color.WHITE);

                            order.getProducts().forEach((barcode, amount) -> {
                                var product = ProductManager.getInstance().getProduct(barcode);
                                pane.add(new JLabel(amount + "x - " + product.getName()));
                            });
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

                        scrollable.add(panel);
                    }

                    grossEarnings.set(grossEarningsDouble);
                }));

                scrollableList.setOpaque(false);
                scrollableList.setPreferredSize(new Dimension(PANEL_WIDTH, 650));
                scrollableList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                scrollableList.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

                statsPanel.add(scrollableList);

                statsPanel.add(new JLabel("Gross Earnings: " + Utils.formatCurrency(grossEarnings.get())));

                statsPanel.repaint();
            };

            secondPanel.add(statsPanel);

            monthsList.addItemListener(e -> {
                createStatsPanel.run();
            });

            if (!monthlyOrders.isEmpty()) {
                createStatsPanel.run();
            } else {
                secondPanel.add(new JLabel("No orders available!"));
            }
        }

        mainPanel.add(secondPanel);
        window.getContentPane().add(mainPanel);

        Main.refresh();
    }

    public static void createManageAccountsScreen(AccountType accountType, Account account) {
        Main.reset();

        AuthManager authManager = accountType == AccountType.ADMINISTRATOR ? Admin.getAuthManager() : Seller.getAuthManager();

        var window = Main.getFrame();
        window.setPreferredSize(new Dimension(1280, 768));
        window.setLocationRelativeTo(null);
        window.pack();

        var mainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mainPanel.setOpaque(false);
        mainPanel.add(Utils.make(createSidebar(account), sidebar -> {
            sidebar.add(Utils.make(new JButton("Create Account"), button -> {
                button.addActionListener(e -> {
                    Main.createRegisterScreen(accountType, $ -> createManageAccountsScreen(accountType, account));
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

            if (authManager.getAccounts().isEmpty()) {
                var panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                panel.setOpaque(false);

                panel.add(Utils.make(new JLabel("No accounts exist!"), label -> {
                    label.setForeground(Color.BLACK);
                }));

                contentPanel.add(panel);
            }

            for (Account acc : authManager.getAccounts()) {
                var panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

                panel.setPreferredSize(new Dimension(PANEL_WIDTH  - 2, 120));
                panel.setMaximumSize(new Dimension(PANEL_WIDTH  - 2, 120));
                panel.setBackground(ColorUtils.fromHex(0x0047D6));
                panel.setBorder(new LineBorder(Color.BLACK, 1, true));

                var infoPanel = new JPanel();
                infoPanel.setOpaque(false);
                infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                infoPanel.add(new JLabel("Email: " + acc.getEmail()));
                infoPanel.add(new JLabel("Name: " + acc.getDisplayName()));

                panel.add(infoPanel);

                panel.add(Utils.make(new JButton(new ImageIcon(Utils.resizeImage("pencil.png", 24, 24))), button -> {
                    button.setPreferredSize(new Dimension(24, 24));
                    button.setToolTipText("Edit");

                    button.addActionListener(e -> {
                        SharedScreens.showAccountDetailsScreen(authManager, acc, account, () -> createManageAccountsScreen(accountType, account));
                    });
                }));

                panel.add(Utils.make(new JButton(new ImageIcon(Utils.resizeImage("trash_bin.png", 24, 24))), button -> {
                    button.setPreferredSize(new Dimension(24, 24));
                    button.setToolTipText("Delete");

                    if (account == acc) {
                        button.setEnabled(false);
                    }

                    button.addActionListener(e -> {
                        authManager.deleteAccount(account);
                        createManageAccountsScreen(accountType, account);
                    });
                }));

                contentPanel.add(panel);
            }

            secondPanel.add(mainScrollPane);
        }

        mainPanel.add(secondPanel);
        window.getContentPane().add(mainPanel);

        Main.refresh();

        Main.refresh();
    }

    public static void createManageCategoriesScreen(Account account) {
        Main.reset();

        var window = Main.getFrame();
        window.setPreferredSize(new Dimension(1280, 768));
        window.setLocationRelativeTo(null);
        window.pack();

        var mainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mainPanel.setOpaque(false);
        mainPanel.add(createSidebar(account));

        var secondPanel = new JPanel();
        secondPanel.setOpaque(false);

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

                panel.setPreferredSize(new Dimension(PANEL_WIDTH - 2, 80));
                panel.setMaximumSize(new Dimension(PANEL_WIDTH - 2, 80));
                panel.setBackground(ColorUtils.fromHex(0x0047D6));
                panel.setBorder(new LineBorder(Color.BLACK, 1, true));

                var infoPanel = new JPanel();
                infoPanel.setOpaque(false);
                infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                infoPanel.add(new JLabel(product.getName()));
                infoPanel.add(new JLabel("EAN: " + product.getBarcode()));
                infoPanel.add(Utils.make(new JComboBox<>(ProductCategory.values()), box -> {
                    box.setSelectedItem(product.getCategory());
                    box.addItemListener(e -> {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            product.setCategory((ProductCategory) e.getItem());
                            ProductManager.getInstance().save();
                        }
                    });
                }));
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
                scrollPane.setPreferredSize(new Dimension(400, 60));
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

                contentPanel.add(panel);
            }

            mainPanel.add(mainScrollPane);
        }

        mainPanel.add(secondPanel);
        window.getContentPane().add(mainPanel);

        Main.refresh();

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
            sidebar.add(makeDropdownPanel("Administrative", java.util.List.of(
                new DropdownItem("Home", () -> createHomeScreen(account)),
                new DropdownItem("Manage Admins", () -> createManageAccountsScreen(AccountType.ADMINISTRATOR, account)),
                new DropdownItem("Manage Sellers", () -> createManageAccountsScreen(AccountType.SELLER, account)),
                new DropdownItem("Manage Categories", () -> createManageCategoriesScreen(account))
            )));
        }

        sidebar.add(Utils.make(new JButton(account.getDisplayName(), new ImageIcon(Utils.getCircularImage(Utils.resizeImage("profile.png", 24, 24)))), button -> {
            button.setOpaque(false);
            button.setToolTipText("View Account Details");

            button.addActionListener(e -> {
                SharedScreens.showAccountDetailsScreen(Admin.getAuthManager(), account, () -> Admin.create(account));
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
