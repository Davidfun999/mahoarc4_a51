// TestAlgorithms.java
// Đặt cùng thư mục với A51.java và RC4.java

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Lớp này dùng để kiểm tra (test) logic của 2 thuật toán RC4 và A5/1
 * một cách độc lập, không cần giao diện hay xử lý tệp tin.
 */
public class TestAlgorithms {

    public static void main(String[] args) {
        // Chạy kiểm tra cho từng thuật toán
        testRC4();
        System.out.println("\n" + "=".repeat(40) + "\n");
        testA51();
    }

    /**
     * Hàm kiểm tra logic mã hóa và giải mã của RC4
     */
    public static void testRC4() {
        System.out.println("--- BẮT ĐẦU KIỂM TRA RC4 ---");

        // 1. Dữ liệu mẫu
        String vanBanGoc = "Chào bạn, đây là thuật toán RC4!";
        byte[] khoa = "KhoaBiMatCuaToi".getBytes(StandardCharsets.UTF_8);
        byte[] duLieuGoc = vanBanGoc.getBytes(StandardCharsets.UTF_8);

        System.out.println("Văn bản gốc: " + vanBanGoc);
        System.out.println("Khóa: " + new String(khoa, StandardCharsets.UTF_8));

        // 2. Mã Hóa
        System.out.println("\nĐang mã hóa...");
        RC4 boMaHoa = new RC4(khoa);
        byte[] banMa = boMaHoa.xuLyDuLieu(duLieuGoc);
        System.out.println("Bản mã (dạng hex): " + bytesSangHex(banMa));

        // 3. Giải Mã
        // QUAN TRỌNG: Phải tạo một đối tượng MỚI với CÙNG MỘT KHÓA
        // để reset Bảng S và bắt đầu dòng khóa lại từ đầu.
        System.out.println("\nĐang giải mã...");
        RC4 boGiaiMa = new RC4(khoa);
        byte[] duLieuGiaiMa = boGiaiMa.xuLyDuLieu(banMa);
        String vanBanGiaiMa = new String(duLieuGiaiMa, StandardCharsets.UTF_8);
        System.out.println("Văn bản đã giải mã: " + vanBanGiaiMa);

        // 4. Kiểm tra
        if (vanBanGoc.equals(vanBanGiaiMa)) {
            System.out.println("\n[THÀNH CÔNG] RC4: Dữ liệu khớp!");
        } else {
            System.out.println("\n[THẤT BẠI] RC4: Dữ liệu KHÔNG khớp!");
        }
        System.out.println("--- KẾT THÚC KIỂM TRA RC4 ---");
    }

    /**
     * Hàm kiểm tra logic tạo dòng khóa và XOR của A5/1
     */
    public static void testA51() {
        System.out.println("--- BẮT ĐẦU KIỂM TRA A5/1 ---");

        // 1. Dữ liệu mẫu
        String vanBanGoc = "Test thuat toan A5/1!";
        // A5/1 yêu cầu khóa 64-bit (8 byte)
        byte[] khoa = new byte[] { 0x01, 0x23, 0x45, 0x67, (byte)0x89, (byte)0xAB, (byte)0xCD, (byte)0xEF };
        // A5/1 yêu cầu số khung 22-bit (chúng ta dùng 3 byte)
        byte[] soKhung = new byte[] { (byte)0xAA, (byte)0xBB, (byte)0xCC };

        byte[] duLieuGoc = vanBanGoc.getBytes(StandardCharsets.UTF_8);
        int soBits = duLieuGoc.length * 8; // Số bit cần để mã hóa

        System.out.println("Văn bản gốc: " + vanBanGoc);
        System.out.println("Khóa (hex): " + bytesSangHex(khoa));
        System.out.println("Số khung (hex): " + bytesSangHex(soKhung));

        // 2. Mã Hóa (Bằng cách tạo dòng khóa 1)
        System.out.println("\nĐang mã hóa (Tạo dòng khóa lần 1)...");
        A51 boMaHoa = new A51();
        boMaHoa.initialize(khoa, soKhung);
        boolean[] keystream1 = boMaHoa.generateKeystream(soBits);

        // Tự thực hiện XOR
        byte[] banMa = xorWithKeystream(duLieuGoc, keystream1);
        System.out.println("Dòng khóa 1 (16 bit đầu): " + A51.bitsToString(Arrays.copyOf(keystream1, 16)));
        System.out.println("Bản mã (dạng hex): " + bytesSangHex(banMa));

        // 3. Giải Mã (Bằng cách tạo dòng khóa 2)
        // QUAN TRỌNG: Phải tạo đối tượng MỚI và initialize() LẠI
        // với CÙNG một khóa và CÙNG một số khung.
        System.out.println("\nĐang giải mã (Tạo dòng khóa lần 2)...");
        A51 boGiaiMa = new A51();
        boGiaiMa.initialize(khoa, soKhung); // Dùng CÙNG khóa và CÙNG số khung
        boolean[] keystream2 = boGiaiMa.generateKeystream(soBits);

        // Tự thực hiện XOR lần nữa
        byte[] duLieuGiaiMa = xorWithKeystream(banMa, keystream2);
        String vanBanGiaiMa = new String(duLieuGiaiMa, StandardCharsets.UTF_8);
        System.out.println("Dòng khóa 2 (16 bit đầu): " + A51.bitsToString(Arrays.copyOf(keystream2, 16)));
        System.out.println("Văn bản đã giải mã: " + vanBanGiaiMa);

        // 4. Kiểm tra
        boolean dataMatch = vanBanGoc.equals(vanBanGiaiMa);
        boolean keystreamMatch = A51.bitsToString(keystream1).equals(A51.bitsToString(keystream2));

        if (dataMatch && keystreamMatch) {
            System.out.println("\n[THÀNH CÔNG] A5/1: Dữ liệu khớp VÀ dòng khóa tạo ra giống hệt nhau!");
        } else {
            System.out.println("\n[THẤT BẠI] A5/1: Dữ liệu hoặc dòng khóa KHÔNG khớp!");
        }
        System.out.println("--- KẾT THÚC KIỂM TRA A5/1 ---");
    }


    // --- CÁC HÀM TRỢ GIÚP ---

    /**
     * Hàm trợ giúp: Chuyển mảng byte sang chuỗi Hex
     */
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesSangHex(byte[] mangBytes) {
        char[] kyTuHex = new char[mangBytes.length * 2];
        for (int j = 0; j < mangBytes.length; j++) {
            int v = mangBytes[j] & 0xFF;
            kyTuHex[j * 2] = HEX_ARRAY[v >>> 4];
            kyTuHex[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(kyTuHex);
    }

    /**
     * Hàm trợ giúp: Dùng để XOR dữ liệu gốc với dòng khóa (keystream)
     * mà thuật toán A5/1 tạo ra.
     * (Đây là logic tương tự như trong FileProcessor)
     */
    private static byte[] xorWithKeystream(byte[] data, boolean[] keystream) {
        if (keystream.length < data.length * 8) {
            throw new IllegalArgumentException("Lỗi: Dòng khóa (keystream) ngắn hơn dữ liệu.");
        }

        byte[] output = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            byte keystreamByte = 0;
            for (int j = 0; j < 8; j++) {
                if (keystream[i * 8 + j]) {
                    keystreamByte |= (1 << j);
                }
            }
            output[i] = (byte) (data[i] ^ keystreamByte);
        }
        return output;
    }
}