// FileEncryptorUI.java
// (Tệp này chứa hàm main để chạy)

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Lớp này chứa TOÀN BỘ mã giao diện người dùng (UI)
 * và hàm main() để khởi chạy ứng dụng.
 */
public class FileEncryptorUI extends JFrame {

    // 1. Các thành phần giao diện
    private JRadioButton radioModeText, radioModeFile;
    private JPanel cardsPanel; // Panel chính dùng CardLayout
    private CardLayout cardLayout;

    // -- Các thành phần chung (dưới cùng) --
    private JPasswordField txtPassword;
    private JTextField txtNonce;
    private JRadioButton radioRC4, radioA51;
    private JRadioButton radioEncrypt, radioDecrypt;
    private JButton btnRun;

    // -- Các thành phần cho Chế độ TEXT --
    private JTextArea txtTextInput;
    private JTextArea txtTextOutput;

    // -- Các thành phần cho Chế độ FILE/FOLDER --
    private JTextField txtFileInputPath;
    private JTextField txtFileOutputPath;
    private JTextArea txtLogArea;
    private boolean isFileMode = true; // Cờ để biết người dùng chọn File hay Folder

    public FileEncryptorUI() {
        setTitle("Chương trình Mã hóa (Text/File/Folder)");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10)); // Layout chính

        // 2. TẠO CÁC PANEL CHÍNH
        JPanel modeSelectionPanel = createModeSelectionPanel();
        JPanel mainPanel = createMainPanel(); // Đây là panel chứa CardLayout + Bảng điều khiển

        // 3. THÊM VÀO FRAME
        add(modeSelectionPanel, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        // Căn lề cho đẹp
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    /**
     * Panel bên trái: Chọn chức năng
     */
    private JPanel createModeSelectionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new TitledBorder("Chọn chức năng"));

        radioModeText = new JRadioButton("Mã hóa Text");
        radioModeFile = new JRadioButton("Mã hóa File/Folder", true); // Mặc định chọn File
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(radioModeText);
        modeGroup.add(radioModeFile);

        // Căn lề trái cho các nút
        radioModeText.setAlignmentX(Component.LEFT_ALIGNMENT);
        radioModeFile.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(radioModeText);
        panel.add(radioModeFile);

        panel.add(Box.createVerticalStrut(20)); // Khoảng đệm

        // Gán hành động để chuyển đổi CardLayout
        radioModeText.addActionListener(e -> cardLayout.show(cardsPanel, "TEXT"));
        radioModeFile.addActionListener(e -> cardLayout.show(cardsPanel, "FILE"));

        panel.add(Box.createVerticalGlue()); // Đẩy các thành phần lên trên
        return panel;
    }

    /**
     * Panel trung tâm (lớn): Chứa CardLayout và Bảng điều khiển
     */
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // -- Trung tâm: Các màn hình chức năng (CardLayout) --
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.add(createTextModePanel(), "TEXT");
        cardsPanel.add(createFileModePanel(), "FILE");

        // -- Dưới cùng: Bảng điều khiển chung --
        JPanel commonControlsPanel = createCommonControlsPanel();

        panel.add(cardsPanel, BorderLayout.CENTER);
        panel.add(commonControlsPanel, BorderLayout.SOUTH);

        // Mặc định hiển thị màn hình FILE
        cardLayout.show(cardsPanel, "FILE");

        return panel;
    }

    /**
     * Card 1: Màn hình cho chế độ "Mã hóa Text"
     */
    private JPanel createTextModePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        txtTextInput = new JTextArea();
        txtTextOutput = new JTextArea();
        txtTextOutput.setEditable(false); // Kết quả không cho sửa
        txtTextOutput.setLineWrap(true);
        txtTextOutput.setWrapStyleWord(true);
        txtTextInput.setLineWrap(true);
        txtTextInput.setWrapStyleWord(true);

        JScrollPane scrollInput = new JScrollPane(txtTextInput);
        scrollInput.setBorder(new TitledBorder("Văn bản gốc (Input)"));

        JScrollPane scrollOutput = new JScrollPane(txtTextOutput);
        scrollOutput.setBorder(new TitledBorder("Kết quả (Output - Base64 nếu mã hóa)"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollInput, scrollOutput);
        splitPane.setResizeWeight(0.5); // Chia đều 50/50

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Card 2: Màn hình cho chế độ "Mã hóa File/Folder"
     */
    private JPanel createFileModePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // -- Panel chọn tệp (trên cùng) --
        JPanel pathPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Dòng 1: Đường dẫn vào
        gbc.gridx = 0; gbc.gridy = 0;
        pathPanel.add(new JLabel("Đầu vào:"), gbc);

        txtFileInputPath = new JTextField(30);
        gbc.gridx = 1; gbc.weightx = 1.0;
        pathPanel.add(txtFileInputPath, gbc);

        JButton btnSelectFile = new JButton("Chọn File");
        gbc.gridx = 2; gbc.weightx = 0;
        pathPanel.add(btnSelectFile, gbc);

        JButton btnSelectFolder = new JButton("Chọn Folder");
        gbc.gridx = 3; gbc.weightx = 0;
        pathPanel.add(btnSelectFolder, gbc);

        // Dòng 2: Đường dẫn ra
        gbc.gridx = 0; gbc.gridy = 1;
        pathPanel.add(new JLabel("Đầu ra:"), gbc);

        txtFileOutputPath = new JTextField(30);
        gbc.gridx = 1;
        pathPanel.add(txtFileOutputPath, gbc);

        JButton btnSelectOutput = new JButton("Chọn...");
        gbc.gridx = 2; gbc.gridwidth = 2; // Gộp 2 cột
        pathPanel.add(btnSelectOutput, gbc);

        // -- Panel Log (giữa) --
        txtLogArea = new JTextArea();
        txtLogArea.setEditable(false);
        txtLogArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScrollPane = new JScrollPane(txtLogArea);
        logScrollPane.setBorder(new TitledBorder("Màn hình hiển thị thông tin"));

        panel.add(pathPanel, BorderLayout.NORTH);
        panel.add(logScrollPane, BorderLayout.CENTER);

        // -- Gán hành động cho các nút chọn tệp --
        btnSelectFile.addActionListener(this::selectPath);
        btnSelectFolder.addActionListener(this::selectPath);
        btnSelectOutput.addActionListener(this::selectPath);

        return panel;
    }

    /**
     * Panel dưới cùng: Chứa các điều khiển chung
     */
    private JPanel createCommonControlsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Bảng điều khiển"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Dòng 1: Key và Nonce
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Mật khẩu (Key):"), gbc);

        txtPassword = new JPasswordField();
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(txtPassword, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(new JLabel("Nonce / Số Khung:"), gbc);

        txtNonce = new JTextField(15);
        gbc.gridx = 3; gbc.weightx = 1.0;
        panel.add(txtNonce, gbc);

        // Dòng 2: Lựa chọn Thuật toán, Chế độ và Nút THỰC HIỆN
        // Panel con cho các RadioButton
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioRC4 = new JRadioButton("RC4", true);
        radioA51 = new JRadioButton("A5/1");
        ButtonGroup algoGroup = new ButtonGroup();
        algoGroup.add(radioRC4); algoGroup.add(radioA51);
        radioPanel.add(radioRC4); radioPanel.add(radioA51);

        radioEncrypt = new JRadioButton("Mã Hóa", true);
        radioDecrypt = new JRadioButton("Giải Mã");
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(radioEncrypt); modeGroup.add(radioDecrypt);

        // Thêm đường kẻ dọc
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(2, 20)); // Đặt kích thước cho separator
        radioPanel.add(separator);

        radioPanel.add(radioEncrypt); radioPanel.add(radioDecrypt);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        panel.add(radioPanel, gbc);

        // Nút THỰC HIỆN (lớn)
        btnRun = new JButton("THỰC HIỆN");
        btnRun.setFont(btnRun.getFont().deriveFont(Font.BOLD, 16f));
        btnRun.setMinimumSize(new Dimension(150, 40));
        gbc.gridx = 3; gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(btnRun, gbc);

        // Gán hành động cho nút chạy
        btnRun.addActionListener(this::runProcessing);
        return panel;
    }

    /**
     * Hành động cho các nút chọn File/Folder
     */
    private void selectPath(ActionEvent e) {
        String command = ((JButton)e.getSource()).getText();
        JFileChooser chooser = new JFileChooser();

        if (command.equals("Chọn File")) {
            isFileMode = true; // Đặt cờ là chế độ FILE
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                txtFileInputPath.setText(f.getAbsolutePath());
                // Tự động gợi ý tên tệp đầu ra
                String outputName = (radioEncrypt.isSelected() ? "encrypted_" : "decrypted_") + f.getName();
                String autoOutput = f.getParent() + File.separator + outputName;
                txtFileOutputPath.setText(autoOutput);
            }
        } else if (command.equals("Chọn Folder")) {
            isFileMode = false; // Đặt cờ là chế độ FOLDER
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                txtFileInputPath.setText(f.getAbsolutePath());
                // Tự động gợi ý thư mục đầu ra
                String outputName = f.getName() + (radioEncrypt.isSelected() ? "_encrypted" : "_decrypted");
                String autoOutput = f.getParent() + File.separator + outputName;
                txtFileOutputPath.setText(autoOutput);
            }
        } else if (command.equals("Chọn...")) {
            // Nút chọn đầu ra
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                txtFileOutputPath.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        }
    }

    /**
     * Hành động chính khi nhấn nút "THỰC HIỆN"
     * (Sử dụng SwingWorker để chạy ngầm không làm đơ UI)
     */
    private void runProcessing(ActionEvent e) {
        // 1. Lấy tất cả thông tin chung
        String password = new String(txtPassword.getPassword());
        String nonce = txtNonce.getText();
        String algorithm = radioRC4.isSelected() ? "RC4" : "A5/1";
        boolean isEncrypt = radioEncrypt.isSelected();

        // Kiểm tra đầu vào chung
        if (password.isEmpty() || nonce.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Mật khẩu và Nonce!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Tắt nút và chuẩn bị xử lý
        btnRun.setEnabled(false);
        btnRun.setText("Đang xử lý...");

        // 3. Quyết định chạy chế độ nào (Text hay File)
        if (radioModeText.isSelected()) {
            // ----- CHẾ ĐỘ TEXT -----
            String inputText = txtTextInput.getText();
            if (inputText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập văn bản!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                btnRun.setEnabled(true);
                btnRun.setText("THỰC HIỆN");
                return;
            }

            // Chạy SwingWorker cho Text
            SwingWorker<String, Void> textWorker = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    // Gọi logic từ FileProcessor
                    return FileProcessor.processText(inputText, password, nonce, algorithm, isEncrypt);
                }

                @Override
                protected void done() {
                    try {
                        String result = get(); // Lấy kết quả từ doInBackground()
                        txtTextOutput.setText(result);
                        txtTextOutput.setCaretPosition(0); // Cuộn lên đầu
                    } catch (Exception ex) {
                        // Xử lý lỗi (ví dụ: giải mã Base64 thất bại)
                        String errorMsg = "LỖI: " + ex.getCause().getMessage();
                        txtTextOutput.setText(errorMsg);
                        JOptionPane.showMessageDialog(FileEncryptorUI.this,
                                "Đã xảy ra lỗi:\n" + ex.getCause().getMessage(),
                                "Lỗi xử lý Text",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    // Dù thành công hay thất bại, bật lại nút
                    btnRun.setEnabled(true);
                    btnRun.setText("THỰC HIỆN");
                }
            };
            textWorker.execute();

        } else {
            // ----- CHẾ ĐỘ FILE/FOLDER -----
            String inputPath = txtFileInputPath.getText();
            String outputPath = txtFileOutputPath.getText();
            if (inputPath.isEmpty() || outputPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn đường dẫn vào và ra!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                btnRun.setEnabled(true);
                btnRun.setText("THỰC HIỆN");
                return;
            }

            txtLogArea.setText("Bắt đầu xử lý...\nThuật toán: " + algorithm + "\n");
            txtLogArea.append(isFileMode ? "Chế độ: 1 Tệp tin\n" : "Chế độ: Thư mục\n");

            // Chạy SwingWorker cho File/Folder
            SwingWorker<Void, Void> fileWorker = new SwingWorker<Void, Void>() {

                // Tạo một triển khai (implementation) của ProgressReporter
                // Nó sẽ gửi các thông điệp đến JTextArea một cách an toàn (thread-safe)
                final ProgressReporter reporter = new ProgressReporter() {
                    @Override
                    public void reportProgress(String message) {
                        // Dùng SwingUtilities để đảm bảo cập nhật UI từ luồng chính
                        SwingUtilities.invokeLater(() -> txtLogArea.append(message + "\n"));
                    }

                    @Override
                    public void reportError(String errorMessage) {
                        SwingUtilities.invokeLater(() -> txtLogArea.append(errorMessage + "\n"));
                    }

                    @Override
                    public void reportCompletion(String message) {
                        SwingUtilities.invokeLater(() -> txtLogArea.append(message + "\n"));
                    }
                };

                @Override
                protected Void doInBackground() throws Exception {
                    if (isFileMode) {
                        // Chế độ 1 File
                        reporter.reportProgress("Đang xử lý tệp: " + inputPath);
                        FileProcessor.processFile(inputPath, outputPath, password, nonce, algorithm);
                    } else {
                        // Chế độ Folder
                        // Giờ đây chúng ta truyền 'reporter' thay vì 'txtLogArea'
                        FileProcessor.processFolder(inputPath, outputPath, password, nonce, algorithm, reporter);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get(); // Kiểm tra xem có lỗi Exception nào ném ra từ doInBackground() không
                        if (isFileMode) {
                            txtLogArea.append(">>> Xử lý tệp thành công! <<<");
                        }
                        // Không cần báo cáo hoàn tất cho thư mục, vì reporter đã làm
                    } catch (Exception ex) {
                        String errorMsg = ">>> LỖI: " + ex.getCause().getMessage() + " <<<";
                        txtLogArea.append(errorMsg);
                        JOptionPane.showMessageDialog(FileEncryptorUI.this,
                                "Đã xảy ra lỗi:\n" + ex.getCause().getMessage(),
                                "Lỗi xử lý File",
                                JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace(); // In ra console để gỡ lỗi
                    }
                    // Dù thành công hay thất bại, bật lại nút
                    btnRun.setEnabled(true);
                    btnRun.setText("THỰC HIỆN");
                }
            };
            fileWorker.execute();
        }
    }

}