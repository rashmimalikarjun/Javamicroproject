package com.abctelecom.nms.view;

import com.abctelecom.nms.model.User;
import com.abctelecom.nms.util.ErrorHelper;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;

/** Screen 2: Login - authenticates via AuthService (PBKDF2 hash verification). */
public class LoginPanel extends JPanel {
    private final JTextField userField = new JTextField(16);
    private final JPasswordField pwdField = new JPasswordField(16);
    private final JButton btnLogin = new JButton("Sign In");
    private final JLabel msg = new JLabel(" ");
    private final Runnable onSuccess;

    public LoginPanel(Runnable onSuccess) {
        this.onSuccess = onSuccess;
        setLayout(new BorderLayout());
        JPanel card = new JPanel();
        card.setBorder(BorderFactory.createTitledBorder("Sign in to QITROM"));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(fieldRow("Username:", userField));
        card.add(fieldRow("Password:", pwdField));
        btnLogin.setAlignmentX(CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> doLogin());
        pwdField.addActionListener(e -> doLogin());
        card.add(Box.createVerticalStrut(8));
        card.add(btnLogin);
        msg.setForeground(new java.awt.Color(150, 40, 40));
        msg.setAlignmentX(CENTER_ALIGNMENT);
        card.add(msg);
        card.add(Box.createVerticalStrut(6));
        card.add(new JLabel("<html><font size='2'>Development bootstrap: run<br>" +
                "java -cp target/qitrom-app.jar:target/lib/* com.abctelecom.nms.BootstrapAdmin<br>" +
                "to create the first administrator account.</font></html>"));
        add(card, BorderLayout.CENTER);
        setPreferredSize(new Dimension(420, 320));
    }

    private JPanel fieldRow(String label, javax.swing.JComponent c) {
        JPanel p = new JPanel(new BorderLayout(6, 2));
        p.setMaximumSize(new Dimension(320, 40));
        p.add(new JLabel(label), BorderLayout.WEST);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private void doLogin() {
        btnLogin.setEnabled(false);
        msg.setText("Authenticating...");
        AppContext.get().async(
                () -> ctx.auth().login(userField.getText(), new String(pwdField.getPassword())),
                (User u) -> {
                    btnLogin.setEnabled(true);
                    pwdField.setText("");
                    onSuccess.run();
                },
                err -> {
                    btnLogin.setEnabled(true);
                    msg.setText(ErrorHelper.message(err));
                });
    }
}
