package GUI;

import Function.*;
import Main.LoginRegisterGUI;
import Main.MainDashboardGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ManagePostsGUI extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel mainRightPanel;
    private final CardLayout editPostCardLayout;
    private final JPanel editPostCardPanel;
    private final CardLayout deletePostCardLayout;
    private final JPanel deletePostCardPanel;
    private boolean editAllSelected;
    private boolean deleteAllSelected;
    private viewPost viewPost;
    private final int authorId;
    private String avatarPath;
    private int PostIdToEdit;
    private int PostIdToDelete;
    private List<JTextField> editTextFields;

    private static final String URL = "jdbc:mysql://localhost:3306/user_management";
    private static final String USER = "root";
    private static final String PASSWORD = "K@miVo_02825";

    public ManagePostsGUI(String username) {
        setTitle("Posts Management System");
        setSize(1600, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // Assuming authorId is retrieved based on the username
        this.authorId = getAuthorIdByUsername(username);
        this.avatarPath = modifyAcc.getAvatarPathId(authorId);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, getWidth(), getHeight());
        layeredPane.setLayout(null);

        JPanel leftPanel = createLeftPanel(username);
        leftPanel.setBounds(0, 0, getWidth() / 3, getHeight());
        layeredPane.add(leftPanel, JLayeredPane.DEFAULT_LAYER);

        cardLayout = new CardLayout();
        mainRightPanel = new JPanel(cardLayout);
        mainRightPanel.setBounds(getWidth() / 3, 0, getWidth() - (getWidth() / 3), getHeight());
        mainRightPanel.setBackground(Color.decode("#2C2C2C"));

        mainRightPanel.add(createWelcomePanel(username), "Main");
        mainRightPanel.add(createAddPostPanel(), "Add Post");
        mainRightPanel.add(createViewPanel(), "View Post");

        editPostCardLayout = new CardLayout();
        editPostCardPanel = new JPanel(editPostCardLayout);
        editPostCardPanel.setOpaque(false);
        editPostCardPanel.add(createEditPostIdPanel(), "EditPostId");
        editPostCardPanel.add(createEditPostOptionsPanel(), "EditPostOptions");
        editPostCardPanel.add(createEditPostPanel(), "EditPost");

        deletePostCardLayout = new CardLayout();
        deletePostCardPanel = new JPanel(deletePostCardLayout);
        deletePostCardPanel.setOpaque(false);
        deletePostCardPanel.add(createDeletePostIdPanel(), "DeletePostId");
        deletePostCardPanel.add(createDeletePostOptionsPanel(), "DeletePostOptions");

        mainRightPanel.add(editPostCardPanel, "Edit Post");
        mainRightPanel.add(deletePostCardPanel, "Delete Post");

        mainRightPanel.add(createManageAccountPanel(username), "Manage Account");

        layeredPane.add(mainRightPanel, JLayeredPane.DEFAULT_LAYER);

        JPanel topEastPanel = createLogoutButtonPanel();
        topEastPanel.setBounds(getWidth() - 130, 10, 90, 35);
        layeredPane.add(topEastPanel, JLayeredPane.PALETTE_LAYER);

        add(layeredPane);
        cardLayout.show(mainRightPanel, "Main");

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int newWidth = getWidth();
                int newHeight = getHeight();
                layeredPane.setBounds(0, 0, newWidth, newHeight);
                leftPanel.setBounds(0, 0, newWidth / 3, newHeight);
                topEastPanel.setBounds(newWidth - 130, 10, 90, 35);
                mainRightPanel.setBounds(newWidth / 3, 0, newWidth - (newWidth / 3), newHeight);
                revalidate();
            }
        });
    }

    private JPanel createLeftPanel(String username) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(20, 0, 20, 0);

        String avatarPath = modifyAcc.getAvatarPathId(authorId);
        JLabel logo = createScaledLogo(avatarPath);

        JLabel userLabel = new JLabel(username);
        userLabel.setFont(new Font("Arial", Font.BOLD, 20));
        userLabel.setForeground(Color.WHITE);

        panel.add(logo, gbc);
        panel.add(userLabel, gbc);

        panel.add(createNavigationButton("Add Post", "Add Post"), gbc);
        styleButton((JButton) panel.getComponent(panel.getComponentCount() - 1), (new Color(0xF38464)), Color.WHITE);

        JButton viewPostButton = createNavigationButton("View Post", "View Post");
        viewPostButton.addActionListener(e -> {
            mainRightPanel.remove(viewPost);
            mainRightPanel.add(createViewPanel(), "View Post");
            cardLayout.show(mainRightPanel, "View Post");
        });
        styleButton(viewPostButton, (new Color(0xF38464)), Color.WHITE);
        panel.add(viewPostButton, gbc);

        panel.add(createNavigationButton("Edit Post", "Edit Post"), gbc);
        styleButton((JButton) panel.getComponent(panel.getComponentCount() - 1), (new Color(0xF38464)), Color.WHITE);

        panel.add(createNavigationButton("Delete Post", "Delete Post"), gbc);
        styleButton((JButton) panel.getComponent(panel.getComponentCount() - 1), (new Color(0xF38464)), Color.WHITE);

        JButton exitButton = createButton("Exit");
        styleButton(exitButton, (new Color(0xF38464)), Color.WHITE);
        exitButton.addActionListener(_ -> {
            new MainDashboardGUI(username).setVisible(true);
            dispose();
        });
        panel.add(exitButton, gbc);

        return panel;
    }

    private JPanel createWelcomePanel(String username) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("Welcome, " + username + "!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        titleLabel.setForeground(Color.WHITE);

        panel.add(titleLabel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAddPostPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Add Title:");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JTextField titleField = new JTextField(20);

        JLabel contentLabel = new JLabel("Add Content:");
        contentLabel.setFont(new Font("Arial", Font.BOLD, 20));
        contentLabel.setForeground(Color.WHITE);

        JTextArea contentArea = new JTextArea(5, 20);

        JButton submitButton = createButton("Submit");
        styleButton(submitButton, (new Color(0xFF0404)), Color.WHITE);
        submitButton.addActionListener(_ -> handleAddPost(titleField.getText(), contentArea.getText()));

        contentArea.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    submitButton.doClick();
                }
            }
        });

        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(titleLabel, gbc);

        gbc.gridx = 1;
        panel.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(contentLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JScrollPane(contentArea), gbc);

        gbc.gridy = 2;
        panel.add(submitButton, gbc);

        return panel;
    }

    private void handleAddPost(String title, String content) {
        if (title.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            try {
                new addPost(title, content, authorId);
                JOptionPane.showMessageDialog(this, "Post added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to add post: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createViewPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        viewPost = new viewPost(authorId);
        panel.add(viewPost, gbc);

        return panel;
    }

    private JPanel createEditPostIdPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Enter Post ID:");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JTextField idField = new JTextField(20);
        idField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    handlePostId(idField.getText());
                    idField.setText("");
                }
            }
        });

        JButton nextButton = createButton("Next");
        styleButton(nextButton, (new Color(0xFF0404)), Color.WHITE);
        nextButton.addActionListener(e -> {
            handlePostId(idField.getText());
            idField.setText("");
        });
        nextButton.setFocusPainted(false);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(titleLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(idField, gbc);

        gbc.gridy = 1;
        gbc.gridx = 1;
        panel.add(nextButton, gbc);

        return panel;
    }

    private void handlePostId(String idText) {
        if(idText.isEmpty()){
            JOptionPane.showMessageDialog(this, "Please fill in the Post ID field", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(idText);

            if(!editPost.isPostExists(id)){
                JOptionPane.showMessageDialog(this, "Post with the given ID does not exist", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            PostIdToEdit = id;
            editPostCardLayout.show(editPostCardPanel, "EditPostOptions");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Post ID", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createEditPostOptionsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;

        JLabel editLabel = new JLabel("Edit Post Options:");
        editLabel.setFont(new Font("Arial", Font.BOLD, 20));
        editLabel.setForeground(Color.WHITE);

        panel.add(editLabel, gbc);

        JCheckBox titleCheckBox = new JCheckBox("Edit Title");
        titleCheckBox.setFont(new Font("Arial", Font.BOLD, 16));

        JCheckBox contentCheckBox = new JCheckBox("Edit Content");
        contentCheckBox.setFont(new Font("Arial", Font.BOLD, 16));

        JCheckBox editAllCheckBox = new JCheckBox("Edit All");
        editAllCheckBox.setFont(new Font("Arial", Font.BOLD, 16));

        titleCheckBox.setOpaque(false);
        titleCheckBox.setForeground(Color.WHITE);

        contentCheckBox.setOpaque(false);
        contentCheckBox.setForeground(Color.WHITE);

        editAllCheckBox.setOpaque(false);
        editAllCheckBox.setForeground(Color.WHITE);

        editAllCheckBox.addActionListener(_ -> {
            boolean selected = editAllCheckBox.isSelected();
            titleCheckBox.setSelected(selected);
            contentCheckBox.setSelected(selected);
            titleCheckBox.setEnabled(!selected);
            contentCheckBox.setEnabled(!selected);
            editAllSelected = selected;
        });
        editAllSelected = editAllCheckBox.isSelected();

        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(titleCheckBox, gbc);

        gbc.gridy++;
        panel.add(contentCheckBox, gbc);

        gbc.gridy++;
        panel.add(editAllCheckBox, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton nextButton = createButton("Next");
        styleButton(nextButton, (new Color(0xFF0404)), Color.WHITE);
        nextButton.addActionListener(_ -> handleEditOption(
                titleCheckBox.isSelected(),
                contentCheckBox.isSelected(),
                editAllCheckBox.isSelected()
        ));
        buttonPanel.add(nextButton);

        JButton backButton = createButton("Back");
        styleButton(backButton, (new Color(0xFF0404)), Color.WHITE);
        backButton.addActionListener(e -> editPostCardLayout.show(editPostCardPanel, "EditPostId"));
        buttonPanel.add(backButton);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private void handleEditOption(boolean editTitle, boolean editContent, boolean editAll) {
        if(!editTitle && !editContent && !editAll) {
            JOptionPane.showMessageDialog(this, "Please select at least one option", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        editPostCardLayout.show(editPostCardPanel, "EditPost");

        JPanel editPanel = (JPanel) editPostCardPanel.getComponent(2);
        editPanel.removeAll();
        editTextFields = new ArrayList<>();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        int gridY = 0;

        if(editTitle || editAll) {

            JLabel newTitleLabel = new JLabel("New Title:");
            newTitleLabel.setFont(new Font("Arial", Font.BOLD, 20));
            newTitleLabel.setForeground(Color.WHITE);

            JTextField newTitleField = new JTextField(20);
            newTitleField.setVisible(true);
            editTextFields.add(newTitleField);

            gbc.gridx = 0;
            gbc.gridy = gridY++;
            gbc.anchor = GridBagConstraints.EAST;
            editPanel.add(newTitleLabel, gbc);

            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.WEST;
            editPanel.add(newTitleField, gbc);
        }

        if(editContent || editAll) {

            JLabel newContentLabel = new JLabel("New Content:");
            newContentLabel.setFont(new Font("Arial", Font.BOLD, 20));
            newContentLabel.setForeground(Color.WHITE);

            JTextField newContentArea = new JTextField(20);
            newContentArea.setVisible(true);
            editTextFields.add(newContentArea);

            gbc.gridx = 0;
            gbc.gridy = gridY++;
            gbc.anchor = GridBagConstraints.EAST;
            editPanel.add(newContentLabel, gbc);

            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.WEST;
            editPanel.add(new JScrollPane(newContentArea), gbc);
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton submitButton = createButton("Submit");
        styleButton(submitButton, (new Color(0xFF0404)), Color.WHITE);
        submitButton.addActionListener(e -> handleEditPost(editAll, editTextFields));
        buttonPanel.add(submitButton);

        JButton backButton = createButton("Back");
        styleButton(backButton, (new Color(0xFF0404)), Color.WHITE);
        backButton.addActionListener(e -> editPostCardLayout.show(editPostCardPanel, "EditPostOptions"));
        buttonPanel.add(backButton);

        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        editPanel.add(buttonPanel, gbc);

        editPanel.revalidate();
        editPanel.repaint();
    }

    private JPanel createEditPostPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        return panel;
    }

    private void handleEditPost(boolean editAll, List<JTextField> editTextFields) {
        String newTitle = null;
        String newContent = null;

        if(editAll) {
            newTitle = editTextFields.get(0).getText();
            newContent = editTextFields.get(1).getText();
        } else {
            for(JTextField field : editTextFields) {
                if(field.isVisible()) {
                    if(field.equals(editTextFields.get(0))) {
                        newTitle = field.getText();
                    } else {
                        newContent = field.getText();
                    }
                }
            }
        }

        if(newTitle == null && newContent == null) {
            JOptionPane.showMessageDialog(this, "Please fill in at least one field", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            new editPost().editPost(PostIdToEdit, authorId,newTitle, newContent);
            JOptionPane.showMessageDialog(this, "Post edited successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to edit post: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createDeletePostIdPanel() {
        JPanel panle = new JPanel(new GridBagLayout());
        panle.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Enter Post ID to Delete:");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JTextField idField = new JTextField(20);
        idField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleDeletePostId(idField.getText());
                    idField.setText("");
                }
            }
        });

        JButton nextButton = createButton("Next");
        styleButton(nextButton, (new Color(0xFF0404)), Color.WHITE);
        nextButton.addActionListener(e -> {
            handleDeletePostId(idField.getText());
            idField.setText("");
        });
        nextButton.setFocusPainted(false);

        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        panle.add(titleLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panle.add(idField, gbc);

        gbc.gridy = 1;
        gbc.gridx = 1;
        panle.add(nextButton, gbc);

        return panle;
    }

    private void handleDeletePostId(String idText) {
        if(idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in the Post ID field", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(idText);

            if(!deletePost.isPostExists(id)) {
                JOptionPane.showMessageDialog(this, "Post with the given ID does not exist", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            PostIdToDelete = id;
            deletePostCardLayout.show(deletePostCardPanel, "DeletePostOptions");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Post ID", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createDeletePostOptionsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;

        JLabel deleteLabel = new JLabel("Delete Post Options:");
        deleteLabel.setFont(new Font("Arial", Font.BOLD, 20));
        deleteLabel.setForeground(Color.WHITE);

        panel.add(deleteLabel, gbc);

       JCheckBox deleteTitleCheckBox = new JCheckBox("Title");
       deleteTitleCheckBox.setFont(new Font("Arial", Font.BOLD, 18));

       JCheckBox deleteContentCheckBox = new JCheckBox("Content");
       deleteContentCheckBox.setFont(new Font("Arial", Font.BOLD, 18));

       JCheckBox deleteAllCheckBox = new JCheckBox("Delete All");
       deleteContentCheckBox.setFont(new Font("Arial", Font.BOLD, 18));

       deleteTitleCheckBox.setOpaque(false);
       deleteTitleCheckBox.setForeground(Color.WHITE);
       deleteContentCheckBox.setOpaque(false);
       deleteContentCheckBox.setForeground(Color.WHITE);
       deleteAllCheckBox.setOpaque(false);
       deleteAllCheckBox.setForeground(Color.WHITE);

       deleteAllCheckBox.addActionListener(_ -> {
           boolean selected = deleteAllCheckBox.isSelected();
           deleteTitleCheckBox.setSelected(selected);
           deleteContentCheckBox.setSelected(selected);
           deleteTitleCheckBox.setSelected(!selected);
           deleteContentCheckBox.setSelected(!selected);
           deleteAllSelected = selected;
       });
       deleteAllSelected = deleteAllCheckBox.isSelected();

       gbc.gridy = 1;
       gbc.anchor = GridBagConstraints.WEST;
       panel.add(deleteTitleCheckBox, gbc);

       gbc.gridy++;
       panel.add(deleteContentCheckBox, gbc);

       gbc.gridy++;
       panel.add(deleteAllCheckBox, gbc);

       JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
       buttonPanel.setOpaque(false);

       JButton deleteButton = createButton("Delete");
       styleButton(deleteButton, (new Color(0xFF0404)), Color.WHITE);
       deleteButton.addActionListener(e -> handleDeleteOption(
               deleteTitleCheckBox.isSelected(),
               deleteContentCheckBox.isSelected(),
               deleteAllCheckBox.isSelected()
       ));
       buttonPanel.add(deleteButton);

       JButton backButton = createButton("Back");
       styleButton(backButton, (new Color(0xFF0404)), Color.WHITE);

       backButton.addActionListener(e -> deletePostCardLayout.show(deletePostCardPanel, "DeletePostId"));
       buttonPanel.add(backButton);

       gbc.gridy++;
       gbc.gridx = 0;
       gbc.gridwidth = 2;
       gbc.anchor = GridBagConstraints.CENTER;
       panel.add(buttonPanel, gbc);

       return panel;
    }

    private void handleDeleteOption(boolean deleteTitle, boolean deleteContent, boolean deleteAll) {
        if (!deleteTitle && !deleteContent && !deleteAll) {
            JOptionPane.showMessageDialog(this, "Please select at least one option", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            boolean success = new deletePost().deletePost(PostIdToDelete, authorId, deleteTitle, deleteContent, deleteAll);
            if (success) {
                JOptionPane.showMessageDialog(this, "Post deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete post.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to delete post: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton createNavigationButton(String text, String targetPanel) {
        JButton button = createButton(text);
        button.setFocusPainted(false);
        if (targetPanel != null) {
            button.addActionListener(e -> cardLayout.show(mainRightPanel, targetPanel));
        }
        return button;
    }

    private JLabel createScaledLogo(String avatarPath) {
        ImageIcon originalIcon = new ImageIcon(avatarPath);
        Image scaledImage = originalIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        JLabel label = new JLabel(new ImageIcon(scaledImage));
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                cardLayout.show(mainRightPanel, "Manage Account");
            }
        });
        return label;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);

        button.setPreferredSize(new Dimension(200, 50));
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);

        return button;
    }

    private JPanel createLogoutButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setOpaque(false);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setPreferredSize(new Dimension(80, 30));
        logoutButton.setFont(new Font("Arial", Font.BOLD, 12));
        logoutButton.setFocusPainted(false);

        logoutButton.addActionListener(_ -> {
            new LoginRegisterGUI().setVisible(true);
            dispose();
        });

        panel.add(logoutButton);

        return panel;
    }

    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setFocusPainted(false);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFont(new Font("Roboto", Font.BOLD, 16));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
    }

    private int getAuthorIdByUsername(String username) {
        String query = "SELECT id, role_id FROM user WHERE username = ?";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int roleId = resultSet.getInt("role_id");
                    if (roleId == 1) {
                        return -1; // Special value for admin
                    } else {
                        return resultSet.getInt("id");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if the user is not found or an error occurs
    }

    private JPanel createManageAccountPanel(String username) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel avatarLabel = new JLabel();
        updateAvatarLabel(avatarLabel, avatarPath);
        avatarLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        avatarLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    String fileExtension = modifyAcc.getFileExtension(selectedFile.getName());

                    // Generate new filename based on author ID
                    String uniqueFilename = authorId + fileExtension;
                    String destinationPath = "src/Resources/avatars/" + uniqueFilename; // Destination path on server

                    // Delete previous avatar
                    String prevAvatarPath =  modifyAcc.getAvatarPathId(authorId);
                    if (prevAvatarPath != null && !prevAvatarPath.equals("src/Resources/avatars/user.png") && !prevAvatarPath.isEmpty()) {
                        File prevAvatarFile = new File(prevAvatarPath);
                        if(prevAvatarFile.exists()) {
                            prevAvatarFile.delete();
                        }
                    }
                    // Resize and copy file to server directory
                    try {
                        BufferedImage originalImage = ImageIO.read(selectedFile);
                        int targetWidth = 200;
                        int targetHeight = 200;
                        BufferedImage resizedImage = resizeImage(originalImage, targetWidth, targetHeight);
                        File output = new File(destinationPath);
                        ImageIO.write(resizedImage,modifyAcc.getFileExtension(destinationPath).replace(".",""), output);
                        updateAvatarLabel(avatarLabel, destinationPath);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        avatarPath = modifyAcc.getAvatarPathId(authorId);
                        updateAvatarLabel(avatarLabel, avatarPath);
                    }
                }
            }
        });

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        usernameLabel.setForeground(Color.WHITE);

        JTextField usernameField = new JTextField(20);

        JLabel ageLabel = new JLabel("Age:");
        ageLabel.setFont(new Font("Arial", Font.BOLD, 20));
        ageLabel.setForeground(Color.WHITE);

        JTextField ageField = new JTextField(20);

        JLabel homeTownLabel = new JLabel("Home Town:");
        homeTownLabel.setFont(new Font("Arial", Font.BOLD, 20));
        homeTownLabel.setForeground(Color.WHITE);

        JTextField homeTownField = new JTextField(20);

        JLabel oldPassLabel = new JLabel("Old Password:");
        oldPassLabel.setFont(new Font("Arial", Font.BOLD, 20));
        oldPassLabel.setForeground(Color.WHITE);

        JPasswordField oldPassField = new JPasswordField(20);

        JLabel newPassLabel = new JLabel("New Password:");
        newPassLabel.setFont(new Font("Arial", Font.BOLD, 20));
        newPassLabel.setForeground(Color.WHITE);

        JPasswordField newPassField = new JPasswordField(20);

        JButton updateButton = createButton("Update");
        styleButton(updateButton, new Color(0xFF0404), Color.WHITE);
        updateButton.addActionListener(_ -> {
            String newName = null;
            String newHometown = null;
            String newAge = null;
            String oldPass = null;
            String newPass = null;

            // Get new values
            if(!usernameField.getText().isEmpty()) {
                newName = usernameField.getText();
            }
            if(!homeTownField.getText().isEmpty()) {
                newHometown = homeTownField.getText();
            }
            if(!ageField.getText().isEmpty()) {
                newAge = ageField.getText();
            }
            if(!new String(newPassField.getPassword()).isEmpty() && !new String(oldPassField.getPassword()).isEmpty() && new String(newPassField.getPassword()).equals(new String(oldPassField.getPassword()))) {
                oldPass = new String(oldPassField.getPassword());
                newPass = new String(newPassField.getPassword());
            }
            handleUpdateAccount(username, newName, newHometown, newAge, oldPass, newPass, avatarPath);

            // Clear fields
            usernameField.setText("");
            homeTownField.setText("");
            ageField.setText("");
            oldPassField.setText("");
            newPassField.setText("");
        });

        newPassField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    updateButton.doClick();
                }
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridwidth = 3;
        panel.add(avatarLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(ageLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(ageField, gbc);


        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(homeTownLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(homeTownField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(oldPassLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(oldPassField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(newPassLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(newPassField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(updateButton, gbc);
        return panel;
    }
    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }

    //update avatar label
    private void updateAvatarLabel(JLabel avatarLabel, String avatarPath) {
        ImageIcon avatarIcon = new ImageIcon(avatarPath);
        Image avatarImage = avatarIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        avatarLabel.setIcon(new ImageIcon(avatarImage));
    }

    private void handleUpdateAccount(String currentUsername, String newUsername, String newHometown, String newAge,String oldPassword, String newPassword, String avatarPath) {
        if(newUsername == null && newHometown == null && newAge == null && oldPassword == null && newPassword == null && avatarPath == null) {
            JOptionPane.showMessageDialog(this, "Please fill in at least one field", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Integer age = null;
            if (newAge != null && !newAge.isEmpty()){
                age = Integer.parseInt(newAge);
            }

            boolean success = new modifyAcc().modifyAcc(authorId, currentUsername, newUsername, newHometown, age, oldPassword, newPassword, avatarPath);

            if(success) {
                if(!currentUsername.equals(newUsername)) {
                    new ManagePostsGUI(newUsername).setVisible(true);
                    dispose();
                }
                JOptionPane.showMessageDialog(this, "Account updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update account", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to update account: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ManagePostsGUI("admin").setVisible(true));
    }
}