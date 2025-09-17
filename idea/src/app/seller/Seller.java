package app.seller;

import app.Main;
import app.auth.Account;
import app.auth.AccountType;
import app.auth.AuthManager;
import app.ui.GraphBuilder;
import app.util.ColorUtils;
import app.util.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Seller {
    private static final AuthManager authManager = new AuthManager(AccountType.SELLER);

    public static AuthManager getAuthManager() {
        return authManager;
    }

    public static void init() {

    }

    public static void create(Account account) {
        Main.reset();

        var window = Main.getFrame();

        window.setPreferredSize(new Dimension(1280, 768));
        window.setLocationRelativeTo(null);
        window.pack();

        var mainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mainPanel.setOpaque(false);
        mainPanel.add(SellerViews.createSidebar(account));

        var contentsPanel = new JPanel(new GridLayout(2, 2));
        contentsPanel.setOpaque(false);

        // Business Insights
        {
            var panel = new JPanel();
            panel.setOpaque(false);
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            panel.add(new JLabel("Business Insights"));

            var graph = new GraphBuilder(450, 250, Color.YELLOW, ColorUtils.fromHex(0x404040), ColorUtils.fromHex(0xADADAD));
            panel.add(Utils.make(new JLabel(new ImageIcon(graph.createImage())), component -> {
                component.setBorder(new EmptyBorder(5, 5, 5, 5));
            }));

            contentsPanel.add(panel);
        }

        mainPanel.add(contentsPanel);

        window.getContentPane().add(mainPanel);

        Main.refresh();
    }
}
