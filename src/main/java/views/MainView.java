package views;

import com.mashape.unirest.http.exceptions.UnirestException;
import controller.Controller;
import dto.LoginUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.Props;
import utils.Props.GlobalProperties;
import utils.UpdateThread;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class MainView extends JFrame {

    // TODO
    // check change sp

    private final static Logger log = LogManager.getLogger(MainView.class);
    private static final long serialVersionUID = 1L;
    JTabbedPane workstationTab = null;
    JTabbedPane receptionTab = new JTabbedPane(JTabbedPane.TOP);
    private JPanel contentPane;
    private TrayIconHandeler tih = new TrayIconHandeler();
    private JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

    /**
     * Create the frame.
     *
     * @param lu login user
     */
    public MainView(LoginUser lu) {

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 390, 452);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        GridBagLayout gbl_contentPane = new GridBagLayout();
        gbl_contentPane.columnWidths = new int[]{0, 0};
        gbl_contentPane.rowHeights = new int[]{0, 0, 0};
        gbl_contentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_contentPane.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
        contentPane.setLayout(gbl_contentPane);


        GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
        gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
        gbc_tabbedPane.fill = GridBagConstraints.BOTH;
        gbc_tabbedPane.gridx = 0;
        gbc_tabbedPane.gridy = 0;
        contentPane.add(tabbedPane, gbc_tabbedPane);

        JPanel panel = new JPanel();
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.fill = GridBagConstraints.BOTH;
        gbc_panel.gridx = 0;
        gbc_panel.gridy = 1;
        contentPane.add(panel, gbc_panel);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[]{5, 0, 5, 0};
        gbl_panel.rowHeights = new int[]{0, 0};
        gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        panel.setLayout(gbl_panel);

        JButton btnNewButton = new JButton("Login to additional desk");
        btnNewButton.addActionListener(arg0 -> {
            LoginFrame frm = new LoginFrame(true, this);
            frm.setVisible(true);
        });
        GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
        gbc_btnNewButton.anchor = GridBagConstraints.WEST;
        gbc_btnNewButton.insets = new Insets(0, 0, 0, 5);
        gbc_btnNewButton.gridx = 1;
        gbc_btnNewButton.gridy = 0;
        panel.add(btnNewButton, gbc_btnNewButton);

        Integer propsForWS = 2; // Default to workstation

        try {
            propsForWS = Integer.valueOf(Props.getGlobalProperty(GlobalProperties.RECEPTION1WORKSTATION2BOTH0));
        } catch (NumberFormatException e) {
        }

        WorkstationPanel main = null;
        ReceptionPanel rp = null;
        if (propsForWS == 0) {
            main = new WorkstationPanel(lu, this);
            rp = new ReceptionPanel(lu, this);
        } else if (propsForWS == 1) {
            rp = new ReceptionPanel(lu, this);
        } else if (propsForWS == 2) {
            main = new WorkstationPanel(lu, this);
        }

        if (main != null) {
            tabbedPane.addTab("Workstation", null, main, null);
        }
        if (rp != null) {
            tabbedPane.addTab("Reception", null, rp, null);
        }

        setResizable(false);
        setVisible(true);
        setLocationRelativeTo(null);
        String appName = Props.getGlobalProperty(GlobalProperties.APP_NAME);
        setTitle(Props.getLangProperty("MainFrame.title") + appName);
        try {
            setIconImage(ImageIO.read(getClass().getClassLoader().getResource("qmaticBigTransparent.png")));
        } catch (IOException e) {
            log.error(e);
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                Controller cont = new Controller();
                try {
                    cont.logout(lu);
                    tih.removeTray();
                    log.info("Logged out and killed unirest");
                } catch (UnirestException | IOException e) {
                    log.error(e);
                }
            }
        });

        UpdateThread updateThread = new UpdateThread(lu, this);
        new Thread(updateThread).start();

        Boolean showCounter = Boolean.valueOf(Props.getGlobalProperty(GlobalProperties.SHOW_COUNTER_OPTIONS));
        Dimension sizeCurrent = getSize();
        if (!showCounter) {
            Dimension newDim = new Dimension((int) sizeCurrent.getWidth(), (int) sizeCurrent.getHeight() - 50);
            setPreferredSize(newDim);
            setSize(newDim);
        }
        tih.displayTray(Props.getLangProperty("noti.loggedIn.title"), Props.getLangProperty("noti.loggedIn.message"),
                MessageType.INFO);
    }

    public TrayIconHandeler getNotification() {
        return tih;
    }

    public void showMessageDialog() {
        JOptionPane.showMessageDialog(this, Props.getLangProperty("MainFrame.ErrorMessage"), "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public void showMessageDialog(String message, int type) {
        JOptionPane.showMessageDialog(this, message, "Error", type);
    }

    public void addAddionalWorkstationPanel(LoginUser addionalUsers) {
        WorkstationPanel wp = new WorkstationPanel(addionalUsers, this);
        tabbedPane.addTab(addionalUsers.getUsername() + " - Workstation", null, wp, null);
    }


}
