// FileProcessor.java
// Đặt cùng thư mục với A51.java và RC4.java

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Stream;

/**
 * Interface (Hợp đồng) để báo cáo tiến trình.
 * Lớp logic (FileProcessor) sẽ GỌI các hàm này.
 * Lớp UI (FileEncryptorUI) sẽ CUNG CẤP cách thực thi (implements) chúng.
 * Điều này giúp FileProcessor không cần "biết" JTextArea là gì.
 */
interface ProgressReporter {
    void reportProgress(String message);
    void reportError(String errorMessage);
    void reportCompletion(String message);
}

/**
 * Lớp này chứa TOÀN BỘ logic xử lý nghiệp vụ (mã hóa/giải mã).
 * Nó không dính dáng gì đến thư viện Swing.
 */
public class FileProcessor {

    /**
     * Xử lý mã hóa/giải mã một chuỗi Text.
     * @param isEncryptMode True = Mã hóa (Text -> Base64), False = Giải mã (Base64 -> Text)
     * @return Chuỗi kết quả
     */
    public static String processText(String inputText, String password, String nonce, String algorithm, boolean isEncryptMode) throws Exception {
        if (isEncryptMode) {
            // --- MÃ HÓA ---
            byte[] inputBytes = inputText.getBytes(StandardCharsets.UTF_8);
            byte[] outputBytes = performCrypto(inputBytes, password, nonce, algorithm);
            // Trả về dạng Base64 để có thể hiển thị
            return Base64.getEncoder().encodeToString(outputBytes);
        } else {
            // --- GIẢI MÃ ---
            byte[] inputBytes;
            try {
                // Dữ liệu vào phải là Base64 hợp lệ
                inputBytes = Base64.getDecoder().decode(inputText);
            } catch (IllegalArgumentException e) {
                throw new Exception("Chuỗi đầu vào không phải là định dạng Base64 hợp lệ.", e);
            }
            byte[] outputBytes = performCrypto(inputBytes, password, nonce, algorithm);
            // Trả về dạng String (UTF-8)
            try {
                return new String(outputBytes, StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new Exception("Giải mã thất bại. Có thể sai Khóa hoặc Nonce.", e);
            }
        }
    }

    /**
     * Xử lý 1 tệp tin duy nhất.
     */
    public static void processFile(String inputFile, String outputFile, String password, String nonce, String algorithm) throws Exception {
        byte[] inputBytes = Files.readAllBytes(Paths.get(inputFile));
        byte[] outputBytes = performCrypto(inputBytes, password, nonce, algorithm);
        Files.write(Paths.get(outputFile), outputBytes);
    }

    /**
     * Xử lý đệ quy toàn bộ thư mục.
     * @param reporter Một đối tượng thực thi 'ProgressReporter' để báo cáo tiến trình.
     */
    public static void processFolder(String inputFolder, String outputFolder, String password, String nonce, String algorithm, ProgressReporter reporter) throws Exception {
        Path inDir = Paths.get(inputFolder);
        Path outDir = Paths.get(outputFolder);

        if (!Files.isDirectory(inDir)) {
            throw new Exception("Đường dẫn đầu vào không phải là một thư mục: " + inputFolder);
        }

        // Đảm bảo thư mục đầu ra tồn tại
        Files.createDirectories(outDir);

        try (Stream<Path> stream = Files.walk(inDir)) {
            stream.filter(Files::isRegularFile).forEach(inPath -> {
                try {
                    // Tính toán đường dẫn tương đối (ví dụ: subfolder/file.txt)
                    Path relativePath = inDir.relativize(inPath);
                    // Tạo đường dẫn tệp tin đầu ra (ví dụ: output_dir/subfolder/file.txt)
                    Path outPath = outDir.resolve(relativePath);

                    // Đảm bảo thư mục cha tồn tại
                    Files.createDirectories(outPath.getParent());

                    // Báo cáo tiến trình qua interface
                    reporter.reportProgress("Đang xử lý: " + relativePath);

                    // Gọi hàm xử lý tệp
                    processFile(inPath.toString(), outPath.toString(), password, nonce, algorithm);

                } catch (Exception e) {
                    // Báo cáo lỗi qua interface
                    reporter.reportError("LỖI xử lý tệp " + inPath.getFileName() + ": " + e.getMessage());
                }
            });
        }

        // Báo cáo hoàn tất
        reporter.reportCompletion("--- HOÀN TẤT XỬ LÝ THƯ MỤC ---");
    }

    /**
     * HÀM LÕI: Thực hiện mã hóa (tách ra từ hàm process cũ)
     * Hàm này lấy byte[] vào và trả về byte[]
     */
    private static byte[] performCrypto(byte[] inputBytes, String password, String nonce, String algorithm) throws Exception {
        byte[] keyBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] nonceBytes = nonce.getBytes(StandardCharsets.UTF_8);

        switch (algorithm) {
            case "RC4":
                MessageDigest shaRC4 = MessageDigest.getInstance("SHA-256");
                shaRC4.update(keyBytes);
                shaRC4.update(nonceBytes);
                byte[] rc4Key = shaRC4.digest(); // Khóa 32-byte
                RC4 rc4 = new RC4(rc4Key);
                return rc4.xuLyDuLieu(inputBytes);

            case "A5/1":
                MessageDigest shaA51 = MessageDigest.getInstance("SHA-256");
                // Rút gọn Mật khẩu -> 8 byte (64-bit)
                byte[] derivedKey = Arrays.copyOf(shaA51.digest(keyBytes), 8);
                shaA51.reset();
                // Rút gọn Nonce -> 3 byte (24-bit, thuật toán sẽ chỉ dùng 22 bit)
                byte[] derivedFrame = Arrays.copyOf(shaA51.digest(nonceBytes), 3);

                A51 a51 = new A51();
                a51.initialize(derivedKey, derivedFrame);
                int numBits = inputBytes.length * 8;
                boolean[] keystream = a51.generateKeystream(numBits);
                return xorWithKeystream(inputBytes, keystream);

            default:
                throw new Exception("Thuật toán không được hỗ trợ: " + algorithm);
        }
    }

    /**
     * Hàm trợ giúp: XOR cho A5/1
     */
    private static byte[] xorWithKeystream(byte[] data, boolean[] keystream) {
        if (keystream.length < data.length * 8) {
            // Đảm bảo dòng khóa đủ dài
            throw new IllegalArgumentException("Lỗi: Dòng khóa (keystream) ngắn hơn dữ liệu.");
        }

        byte[] output = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            byte keystreamByte = 0;
            for (int j = 0; j < 8; j++) {
                // Lấy bit từ dòng khóa
                if (keystream[i * 8 + j]) {
                    // "Đóng gói" 8 bit boolean thành 1 byte
                    // Bit LSB (j=0) ứng với bit 0 của byte
                    keystreamByte |= (1 << j);
                }
            }
            // XOR byte dữ liệu với byte dòng khóa
            output[i] = (byte) (data[i] ^ keystreamByte);
        }
        return output;
    }
}