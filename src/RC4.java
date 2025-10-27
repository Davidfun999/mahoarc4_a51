public class RC4 {
    // khởi tạo một mảng có kích thước cố định 256
    private static final int trangThai = 256;
    private int[] S;

    // Các biến trạng thái cho Giai đoạn sinh khóa
    private int i_sinhKhoa;
    private int j_sinhKhoa;

    // hàm khởi tạo
    public RC4(byte[] khoa){
        this.i_sinhKhoa = 0;
        this.j_sinhKhoa = 0;
        khoiTaoBang();
        hoanViBangS(khoa);
    }
    // hàm khởi tạo bảng S và gán giá trị S[i]
    public void khoiTaoBang(){
        this.S = new int[trangThai];
        for(int i = 0; i < trangThai;i++){
            this.S[i] = i;
        }
    }
    // hàm hoán vị i và j
    public void hoanVi(int i,int j){
        int temp = this.S[i];
        this.S[i] = this.S[j];
        this.S[j] = temp;
    }
    //j = 0
    //for i from 0 to 255:
    //  j = (j + S[i] + key[i % key_length]) % 255
    //  swap values of S[i] and S[j]
    public void hoanViBangS(byte[] khoa) {
        int j = 0;
        int doDaiKhoa = khoa.length;
        for (int i = 0; i < trangThai; i++) {
            // (khoa[i % doDaiKhoa] & 0xFF) -> chuyển byte có dấu thành int không dấu
            j = (j + this.S[i] + (khoa[i % doDaiKhoa] & 0xFF)) % trangThai;
            hoanVi(i, j);
        }
    }
    //i := 0
    //j := 0
    //while output_length < plaintext_length:
    //      i := (i + 1) mod 8
    //      j := (j + S[i]) mod 8
    //      swap values of S[i] and S[j]
    //      K := S[(S[i] + S[j]) mod 8]
    //      output K
    public byte[] xuLyDuLieu(byte[] duLieu ){
        byte[] result = new byte[duLieu.length];
        // lặp qua từng byte của dữ liệu vào
        for(int i =0; i < duLieu.length;i++){
            this.i_sinhKhoa = (this.i_sinhKhoa +1) % trangThai;
            this.j_sinhKhoa = (this.j_sinhKhoa + this.S[this.i_sinhKhoa]) % trangThai;
            hoanVi(this.i_sinhKhoa,this.j_sinhKhoa);
            int temp = (this.S[this.i_sinhKhoa] + this.S[this.j_sinhKhoa]) % trangThai ;
            int byteDongKhoa = this.S[temp];
            // kết thúc 1 vòng

            // XOR
            result[i] = (byte)( duLieu[i] ^ byteDongKhoa);
        }
        return result;
    }
}
