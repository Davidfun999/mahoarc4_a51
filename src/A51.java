import java.util.Arrays;
public class A51 {
    // 1. TRẠNG THÁI (CÁC THANH GHI)
    private boolean[] X = new boolean[19];
    private boolean[] Y = new boolean[22];
    private boolean[] Z = new boolean[23];

    // 2. CÁC HÀM LOGIC CƠ BẢN
    private boolean xor(boolean a, boolean b) {
        return a != b;
    }

    private boolean xor(boolean... bits) {
        boolean result = false;
        for (boolean bit : bits) {
            result = xor(result, bit);
        }
        return result;
    }

    private boolean majority(boolean a, boolean b, boolean c) {
        return (a && b) || (a && c) || (b && c);
    }

    // 3. CÁC HÀM CLOCK CHI TIẾT
    // Các hàm này CHỈ tính toán bit phản hồi và dịch chuyển thanh ghi.

    /**
     * Clock thanh ghi X (19 bit).
     * Phản hồi: t = X[18] ⊕ X[17] ⊕ X[16] ⊕ X[13]
     */
    private void clockX() {
        boolean t = xor(X[18], X[17], X[16], X[13]);
        // Dịch chuyển: X[17] -> X[18], X[16] -> X[17], ..., X[0] -> X[1]
        System.arraycopy(X, 0, X, 1, 18);
        X[0] = t; // Bit mới vào LSB
    }

    /**
     * Clock thanh ghi Y (22 bit).
     * Phản hồi: t = Y[21] ⊕ Y[20]
     */
    private void clockY() {
        boolean t = xor(Y[21], Y[20]);
        // Dịch chuyển
        System.arraycopy(Y, 0, Y, 1, 21);
        Y[0] = t;
    }

    /**
     * Clock thanh ghi Z (23 bit).
     * Phản hồi: t = Z[22] ⊕ Z[21] ⊕ Z[20] ⊕ Z[7]
     */
    private void clockZ() {
        boolean t = xor(Z[22], Z[21], Z[20], Z[7]);
        // Dịch chuyển
        System.arraycopy(Z, 0, Z, 1, 22);
        Z[0] = t;
    }

    /**
     * Clock cả 3 thanh ghi. Dùng trong quá trình nạp khóa và số khung.
     */
    private void clockAll() {
        clockX();
        clockY();
        clockZ();
    }

    /**
     * Lấy bit đầu ra.
     * Output = X[18] ⊕ Y[21] ⊕ Z[22] (XOR 3 bit MSB)
     */
    private boolean getOutputBit() {
        return xor(X[18], Y[21], Z[22]);
    }

    /**
     * Thực hiện MỘT bước clock "stop-and-go" theo quy tắc đa số.
     * Đây là cơ chế clock chính dùng trong giai đoạn làm nóng và tạo keystream.
     */
    private void step() {
        // 1. Lấy các bit điều khiển (clocking bits)
        boolean mX = X[8];
        boolean mY = Y[10];
        boolean mZ = Z[10];

        // 2. Tính giá trị đa số
        boolean m = majority(mX, mY, mZ);

        // 3. Quyết định clock
        if (mX == m) {
            clockX();
        }
        if (mY == m) {
            clockY();
        }
        if (mZ == m) {
            clockZ();
        }
    }

    // 4. HÀM KHỞI TẠO (QUAN TRỌNG NHẤT)

    /**
     * Khởi tạo trạng thái A5/1 với khóa (64-bit) và số khung (22-bit).
     * @param key 64-bit (8 byte)
     * @param frameNumber 22-bit (thường được truyền trong 3 byte, ví dụ: 2 byte + 6 bit)
     */
    public void initialize(byte[] key, byte[] frameNumber) {
        // Xóa tất cả thanh ghi về 0
        Arrays.fill(X, false);
        Arrays.fill(Y, false);
        Arrays.fill(Z, false);

        // --- Giai đoạn 1: Nạp 64-bit Khóa (Key) ---
        // Với mỗi bit của khóa, clock cả 3 thanh ghi và XOR bit khóa vào LSB
        for (int i = 0; i < 64; i++) {
            clockAll(); // Clock cả 3
            // Lấy bit thứ i của khóa
            boolean keyBit = ((key[i / 8] >> (i % 8)) & 1) == 1;

            // XOR vào LSB của mỗi thanh ghi
            X[0] = xor(X[0], keyBit);
            Y[0] = xor(Y[0], keyBit);
            Z[0] = xor(Z[0], keyBit);
        }

        // --- Giai đoạn 2: Nạp 22-bit Số khung (Frame Number) ---
        // Tương tự, với mỗi bit của số khung
        for (int i = 0; i < 22; i++) {
            clockAll(); // Clock cả 3
            // Lấy bit thứ i của số khung
            boolean frameBit = ((frameNumber[i / 8] >> (i % 8)) & 1) == 1;

            // XOR vào LSB của mỗi thanh ghi
            X[0] = xor(X[0], frameBit);
            Y[0] = xor(Y[0], frameBit);
            Z[0] = xor(Z[0], frameBit);
        }

        // --- Giai đoạn 3: "Làm nóng" (Warm-up) ---
        // Chạy 100 chu kỳ clock theo quy tắc đa số, bỏ qua kết quả
        for (int i = 0; i < 100; i++) {
            step();
        }
    }

    // 5. HÀM TẠO KEYSTREAM

    /**
     * Tạo ra một dòng khóa (keystream) có độ dài cho trước.
     * HÀM NÀY PHẢI ĐƯỢC GỌI SAU KHI ĐÃ GỌI initialize()
     * * @param numBits Số lượng bit keystream cần tạo
     * @return Mảng boolean chứa keystream
     */
    public boolean[] generateKeystream(int numBits) {
        boolean[] keystream = new boolean[numBits];
        for (int i = 0; i < numBits; i++) {
            // 1. Clock theo quy tắc đa số
            step();
            // 2. Lấy bit đầu ra
            keystream[i] = getOutputBit();
        }
        return keystream;
    }

    // 6. HÀM MAIN ĐỂ CHẠY THỬ

    /**
     * Hàm trợ giúp để in mảng boolean (0 hoặc 1)
     */
    public static String bitsToString(boolean[] bits) {
        StringBuilder sb = new StringBuilder(bits.length);
        for (boolean bit : bits) {
            sb.append(bit ? '1' : '0');
        }
        return sb.toString();
    }
}
