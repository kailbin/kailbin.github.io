package lbs;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * 度分秒转换
 * <p>
 * Created by kail on 2017/2/23.
 */
public class DFSUtil {

    private static final long UNIT = 60L;
    private static final String FORMAT = "%d° %d' %d\"";

    /**
     * 1度 = 60分，1分=60秒，1度=3600秒
     * <p>
     * 度 + (分+(秒/60))/60 = 度 + 分/60 + 秒/60/60 = d+(f+(m/60))/60
     *
     * @param d 度
     * @param f 分 < 60
     * @param m 秒 < 60
     */
    private static BigDecimal dfs2num(BigDecimal d, BigDecimal f, BigDecimal m) {
        return d.add(f.add(m.divide(BigDecimal.valueOf(UNIT), MathContext.DECIMAL128)).divide(BigDecimal.valueOf(UNIT), MathContext.DECIMAL128));
    }

    /**
     * 数转 度分秒
     */
    private static String num2dfm(BigDecimal num) {
        long du = num.longValue(); // 度
        BigDecimal f = num.subtract(BigDecimal.valueOf(du)).multiply(BigDecimal.valueOf(UNIT)); // 度取小数位 * 60 = 分
        BigDecimal m = f.subtract(BigDecimal.valueOf(f.intValue())).multiply(BigDecimal.valueOf(UNIT)); // 分取小数位 * 60 = 秒
        return String.format(FORMAT, du, f.intValue(), m.setScale(0, BigDecimal.ROUND_HALF_UP).intValue()); // 时分秒取整数位
    }


    public static void main(String[] args) {
        System.out.println(dfs2num(BigDecimal.ONE, BigDecimal.valueOf(30), BigDecimal.valueOf(30)));
        System.out.println(num2dfm(BigDecimal.valueOf(1.5083333333333333333333333333333333)));
        System.out.println(num2dfm(BigDecimal.valueOf(123.345)));

    }

}
