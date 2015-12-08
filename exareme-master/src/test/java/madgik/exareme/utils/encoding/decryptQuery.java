package madgik.exareme.utils.encoding;

import madgik.exareme.common.app.engine.AdpDBSelectOperator;

import java.io.IOException;

public class decryptQuery {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //Query string extracted from json plan
        String query = "H4sIAAAAAAAAAJVWz28bRRR-XtuJk7SNm0BJKhVZtA0pIG_cFgmpSBA7qbC0JC02HIxAjL3jZMv-ysxssuEQUQnBAYkTHDggceCYQy_8AYgDh55A6oUT6hnOPcCBNzNr7zZJXdeH1e7sm7ff-77vvfHhP1DkDK57xN5yPq3SmDDq0Wov8LzAr5IwrFJ_y_FpddUO1-ot6tKe2AwpIyJgoH85AyabcIpT5hB3I_K6lDVhRgSCuE0_jATHl-ppMxLy0YIJR60LmLfukF1iRsJxzXcI336XhDcsmAx04A4cQM6C4k5E2b6Ay5YGaSYgTQ3S5L1t6hFTY8PtBbEfUgHXnxCONZm6JlPVNKimjbtuxCGkPwOJOSsBViXAagKwOPnnL7-e--T3PBg3YdoNiH2T9HB_E6bENqN8O3DtOHzrbZWivFeSV3kbq3qKyYtTwxc5AUWXuKT2hPcpCpf4W9WmL-gWZXMPf_zp0d2v3jAg14TiLnEjGjMop3FaiC8Pv7sw8-1fXxv4fVWa-si0_Ky8Oa0ucYyfuHiyAzS5VU1uKviEBSUZQX0p45yWUX7XbAnm-Fsow4wSuU26LsWQ5zNKrzJG9i2HS7HOZKKQ3ETyGe0AtSxgebTwKuoDh-7JdCFhnNqtHfe2Ns210XtpHKJk3MGV1m1raKFZZbmWIIImJZYz-BV0ZPulkZQpACljpSYYjt2BCR4wQe0Mf7LmWazZJoJ0CadrDkuWSh6aFdkchOhOSB6mWeRv-rcI0xnKWHy6IumQq_NadfQYCJg0hReadjcUsMhVqZVXKn0WeBWGytHl2srKyhW0wnzq-aFU8d0_Lnz_G_khL_1W4M5nVGfeK2hvM1hKdzUCV2ZHVvml930vsJ2-I0WSif6bvVz7-e9vyobUueDiisbJ4LWnJ0jXF-vw-f2PH72o0uTQmucyAqVh2NAy_Xl1EYn9F7PAs00Hqulyw4pG6ju0XapxsQmzfuRt9qUGjoKP0ywkQlDmC3h9bC-uoRVu6W3oxrkkA5YVef4G8ehA8aLQHXJpjA6RLRlhb-gsOoPS0DhgcG1kqSmyagbZYEzOIVsMzqSjZx05yL5EvxU84u8Pyc8p8tGURq8miR7dSAp9SnIB551Ld6nbwUlPvRBd5CMlSVec5jvuGu07vqJfLSYzXd6XBJxvvLe-2l6vtFfr1npFDd7lRq3S3GhfOW4OBrVxiRmOj0zLd6Dk8FaPOaHo4DzkbYQbMML2m7Dgq_m82deHYtYwZTUSM0RLWBctOKuH4vEX5XCwO6styo3YIldP0YSeqYjjPGnXLT7oupVnKPDISMs_dghcHdvdjWQPOnJKQucomJ8AzKN-iWr8GcANUqbgDOwOFw_5ARkG7-LYO3ZQ1SPHtSnDOXGS-i-kpn4s_M0H_z7Xse7cz0t36_CIgfFh495S_97Dj744HJy3ZXjKT8-nBXVvHMj7JRzUuw4TEVFELB_tGvn4aqzcfPI0OxK7EGN_Xx3v_132v1C2g2WelwWU8IBRHvgfkKd6tzYKAAA";
        //Decode
        AdpDBSelectOperator dbOp = Base64Util.decodeBase64(query);

        //Extracting information example...
        System.out.println(dbOp.getOutputTables());
        System.out.println(dbOp.getInputTables());
        System.out.println(dbOp.getQuery().getDatabaseDir());
        System.out.println(dbOp.getQuery().getQuery());
        return;
    }
}
