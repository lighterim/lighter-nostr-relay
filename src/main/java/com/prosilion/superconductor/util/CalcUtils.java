package com.prosilion.superconductor.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalcUtils {

    static BigDecimal BP = BigDecimal.valueOf(10000L);

    /**
     * 计算包含手续费的金额并向上取整
     * 对应 TS: (amount * BigInt(10000 + bp) + 9999n) / 10000n
     * * @param amount      原始金额
     * @param feeRateBp   手续费基点 (1 bp = 0.01%)
     * @return 向上取整后的结果
     */
    public static BigDecimal ceilAmountWithFeeBp(BigDecimal amount, int feeRateBp) {
        if (amount == null) return BigDecimal.ZERO;

        // 1. 处理 BP，确保不小于 0
        int bp = Math.max(0, feeRateBp);

        // 2. 计算乘数 (10000 + bp)
        BigDecimal multiplier = BP.add(BigDecimal.valueOf(bp));

        // 4. 执行计算：(amount * multiplier) / divisor
        // 使用 RoundingMode.CEILING 对应原逻辑中的“向上取整”
        // scale 设置为 0 表示结果不保留小数（模拟原 bigint 的整数行为）
        return amount.multiply(multiplier)
                .divide(BP, 0, RoundingMode.CEILING);
    }

    public static void main(String[] args) {
        // 测试案例
        BigDecimal result = ceilAmountWithFeeBp(new BigDecimal("1123456"), 1);
        // (100 * 10001 + 9999) / 10000 = 101
        System.out.println("Result: " + result); //1123569
    }
}
