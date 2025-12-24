package com.mobile.smartcalling.common;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ChineseAffirmativeChecker{

    // 编译所有正则表达式模式，提高性能
    private static final Pattern[] AFFIRMATIVE_PATTERNS = {
            // 直接表达肯定：有啊
            Pattern.compile("^[^没]*有啊"),
            // 表达有需要：有需要（排除"没有需要"）
            Pattern.compile("^((?!没有需要).)*有需要"),
            // 表达有需求
            Pattern.compile("^[^没]*需求"),
            // 直接表达需要（排除"不"和"没"开头）
            Pattern.compile("^[^(不|没)]*需要"),
            // 直接肯定：没问题
            Pattern.compile("没问题"),
            // 再次表达需要（排除"不"和"没"开头）
            Pattern.compile("^[^(不|没)]*需要"),
            // 表达同意：行/好/是/可以/对 + 语气词
            Pattern.compile("^[^不|没]*(行|好|是|可以|对)(啊|呀|行|吧|的|得)"),
            // 表达可以（排除"不"和"没"开头）
            Pattern.compile("^[^(不|没)]*可以"),
            // 肯定回应：嗯（排除包含"不"或"没"）
            Pattern.compile("^(?!.*(?:不|没)).*嗯.*"),
            // 表达同意：行/好/是/可以/对（无语气词）
            Pattern.compile("^[^不|没]*(行|好|是|可以|对)"),
            // 鼓励提问：问吧
            Pattern.compile("问吧"),
            // 鼓励表达：你说
            Pattern.compile("你说"),
            // 催促表达：抓紧/赶快/赶紧
            Pattern.compile("(抓|赶)(快|紧)"),
            // 催促表达：快讲
            Pattern.compile("快讲")
    };

    private static final Pattern[] NEGATIVE_PATTERNS = {
            // 直接表达否定：不知道
            Pattern.compile("不知道"),
            // 表达不清楚（排除"听不清楚"和"说不清楚"）
            Pattern.compile("^((?!听不清楚|说不清楚).)*不清楚"),
            // 表达不了解
            Pattern.compile("不了解"),
            // 表达暂时不需要
            Pattern.compile("暂时"),
            // 表达目前不需要
            Pattern.compile("目前"),
            // 表达用不着
            Pattern.compile("用不着"),
            // 表达用不到
            Pattern.compile("用不到"),
            // 表达拒绝：不了
            Pattern.compile("不了"),
            // 表达打错电话
            Pattern.compile("打错"),
            // 表达没兴趣
            Pattern.compile("没兴趣"),
            // 表达搞错
            Pattern.compile("搞错"),
            // 表达不好
            Pattern.compile("不好"),
            // 表达不感兴趣
            Pattern.compile("不感兴趣"),
            // 表达没有需要
            Pattern.compile("没.*需要"),
            // 表达不用
            Pattern.compile("不用"),
            // 表达不需要
            Pattern.compile("不需要"),
            // 表达没兴趣（变体）
            Pattern.compile("没.*兴趣"),
            // 表达不愿意
            Pattern.compile("不愿意"),
            // 表达不要
            Pattern.compile("不要"),
            // 表达不考虑
            Pattern.compile("不考虑"),
            // 表达不想
            Pattern.compile("不想"),
            // 表达不加
            Pattern.compile("不加"),
            // 表达已经有了
            Pattern.compile("已经.*(有|过)"),
            // 表达加过
            Pattern.compile("加过"),
            // 表达没时间
            Pattern.compile("没时间"),
            // 表达"我说我没..."
            Pattern.compile("我说我没"),
            // 表达"我说我不..."
            Pattern.compile("我说我不"),
            // 表达不可以
            Pattern.compile("不可以"),
            // 表达不行
            Pattern.compile("不行"),
            // 表达不做
            Pattern.compile("不做"),
            // 表达不必
            Pattern.compile("不必"),
            // 表达没必要
            Pattern.compile("没必要"),
            // 表达没有
            Pattern.compile("没有"),
            // 表达不+需要/可以/好/用/方便/必/做
            Pattern.compile("不.*(需要|可以|好|用|方便|必|做)"),
            // 表达正在忙（开车/开会/上班/吃饭）
            Pattern.compile("(开车|开会|上班|吃饭)"),
            // 表达不+知道/了解/可以/好/用
            Pattern.compile("不.*(知道|了解|可以|好|用)"),
            // 表达用不到/用不着
            Pattern.compile("用不*.(到|着)"),
            // 表达忙（排除"不忙"）
            Pattern.compile("^[^不]*忙")
    };

    private static final Pattern[] SATISFY_PATTERNS = {
            Pattern.compile("^[^(不|没)]*(好|满意|行|可以)"),
            // 表达忙（排除"不忙"）
            Pattern.compile("(很好|不错)")
    };

    private static final Pattern[] DISSATISFY_PATTERNS = {
            // 直接负面评价
            Pattern.compile("(不满意|糟糕|不好|烂|一般|不行|差)"),

            // 安装相关负面
            Pattern.compile("没装好"),
            Pattern.compile("装不好"),

            // 明确拒绝
            Pattern.compile("否"),

            // 委婉负面
            Pattern.compile("就那样"),
            Pattern.compile("不太行"),
            Pattern.compile("不太好")
    };

    private static final Pattern[] SPEEDCONFRIM_PATTERNS = {
            Pattern.compile("^[^不|没|未]*(测|行|好|是|可以|对|有|告诉|告知|确认|说|看|来)")
    };

    private static final Pattern[] NOTSPEEDCONFRIM_PATTERNS = {
            // 否定词+行为动词组合
            Pattern.compile("(没|不|未).*(测|展示|确认|知道|有|说|邀请|了解|听过|问过|考虑|想|弄|搞|做|感兴趣|需要|来)"),

            // 表达不清楚（排除"听不清楚"和"说不清楚"）
            Pattern.compile("^((?!听不清楚|说不清楚).)*不清楚"),

            // 明确未完成
            Pattern.compile("未确认"),
            Pattern.compile("未告知"),

            // 明确拒绝
            Pattern.compile("用不着"),
            Pattern.compile("用不到"),
            Pattern.compile("否")
    };

    private static final Pattern[] APPRAISE_PATTERNS = {
            Pattern.compile("(有|告知|是|好|嗯|说过)")
    };

    private static final Pattern[] NOTAPPRAISE_PATTERNS = {
            Pattern.compile("(没|没说过|不知道|不清楚|未告知)")
    };

    private static final Pattern[] ISHOME_PATTERNS = {
            Pattern.compile("^[^不|没|未]*(有|好|是|可以|对|说|处理|来|弄|修|上门|来)")
    };

    private static final Pattern[] NOTHOME_PATTERNS = {
            Pattern.compile("(没|不|未).*(上门|弄|修|处理|知道|有|说|了解|听过|问过|考虑|想|搞|做|感兴趣|需要|要|来)")
    };

    private static final Pattern[] ISHOMEIDEA_PATTERNS = {
            Pattern.compile("^((?!不|没|未).)*?(有|好|是|可以|对|说|告知|告诉|预约|主动|约过|清楚|来)")
    };

    private static final Pattern[] NOTHOMEIDEA_PATTERNS = {
            // 否定词+行为动词组合：(没|不|未).*(上门|预约|知道|...)
            Pattern.compile("(没|不|未).*(上门|预约|知道|有|说过|约|了解|听过|清楚|问过|考虑|想|弄|搞|做|感兴趣|需要|来|说)"),

            // 表达不清楚（排除"听不清楚"和"说不清楚"）：^((?!听不清楚|说不清楚).)*不清楚
            Pattern.compile("^((?!听不清楚|说不清楚).)*不清楚")
    };

    private static final Pattern[] INVITECOMMENT_PATTERNS = {
            Pattern.compile("^((?!不|没|未).)*?(有|好|是|可以|对|说|告知|告诉|处理|邀请|邀评|听过|了解|评)")
    };

    private static final Pattern[] NOTINVITECOMMENT_PATTERNS = {
            // 否定词+行为/评价动词组合：(没|不|未).*(邀评|知道|...)
            Pattern.compile("(没|不|未).*(邀评|知道|有|说过|邀请|了解|听过|问过|考虑|想|弄|搞|做|感兴趣|需要|要|告知|告诉|评)"),

            // 表达不清楚（排除"听不清楚"和"说不清楚"）：^((?!听不清楚|说不清楚).)*不清楚
            Pattern.compile("^((?!听不清楚|说不清楚).)*不清楚")
    };

    private static final Pattern[] NOTHOMEQ7_PATTERNS = {
            // 不(着|到)
            Pattern.compile("不(着|到)"),

            // 不(了|需要|用|可以|好|行|考虑|感兴趣|愿意|必)
            Pattern.compile("不(了|需要|用|可以|好|行|考虑|感兴趣|愿意|必)"),

            // 没(必要|有|需要|兴趣)
            Pattern.compile("没(必要|有|需要|兴趣)")
    };

    private static final Pattern GETTIME_PATTERNS =
            Pattern.compile("(现在|后天|大后天|今天|明天|周末|上午|下午|晚上|下周|周一|周二|周三|周四|周五|周六|周天|周日)");

    public static String checkTime(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        Matcher matcher = GETTIME_PATTERNS.matcher(input);
        String lastKeyword = null;

        // 查找所有匹配项，只保留最后一个
        while (matcher.find()) {
            lastKeyword = matcher.group();
        }

        return lastKeyword;
    }

        /**
         * 匹配肯定关键词
         * @param input
         * @return boolean
         */
    public static boolean checkAffirmative(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // 去除首尾空白字符
        String trimmedInput = input.trim();

        // 依次检查每个正则表达式
        for (Pattern pattern : AFFIRMATIVE_PATTERNS) {
            if (pattern.matcher(trimmedInput).find()) {
                return true;
            }
        }

        return false;
    }

        /**
         * 匹配否定关键词
         * @param input
         * @return boolean
         */
    public static boolean checkNegative(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // 去除首尾空白字符
        String trimmedInput = input.trim();

        // 依次检查每个正则表达式
        for (Pattern pattern : NEGATIVE_PATTERNS) {
            if (pattern.matcher(trimmedInput).find()) {
                return true;
            }
        }

        return false;
    }

        /**
         * 匹配满意关键词
         * @param input
         * @return boolean
         */
    public static boolean checkSatisfy(String input) {

        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // 去除首尾空白字符
        String trimmedInput = input.trim();

        // 依次检查每个正则表达式
        for (Pattern pattern : SATISFY_PATTERNS) {
            if (pattern.matcher(trimmedInput).find()) {
                return true;
            }
        }

        return false;
    }
        /**
         * 匹配不满意关键词
         * @param input
         * @return boolean
         */
    public static boolean checkDisSatisfy(String input) {

        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // 去除首尾空白字符
        String trimmedInput = input.trim();

        // 依次检查每个正则表达式
        for (Pattern pattern : DISSATISFY_PATTERNS) {
            if (pattern.matcher(trimmedInput).find()) {
                return true;
            }
        }

        return false;
    }
        /**
         * 匹配是否测速关键词
         * @param input
         * @return boolean
         */
    public static boolean checkSpeedConfrim(String input) {

        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // 去除首尾空白字符
        String trimmedInput = input.trim();

        // 依次检查每个正则表达式
        for (Pattern pattern : SPEEDCONFRIM_PATTERNS) {
            if (pattern.matcher(trimmedInput).find()) {
                return true;
            }
        }

        return false;
    }
        /**
         * 匹配是否测速关键词
         * @param input
         * @return boolean
         */
    public static boolean checkNotSpeedConfrim(String input) {

        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // 去除首尾空白字符
        String trimmedInput = input.trim();

        // 依次检查每个正则表达式
        for (Pattern pattern : NOTSPEEDCONFRIM_PATTERNS) {
            if (pattern.matcher(trimmedInput).find()) {
                return true;
            }
        }

        return false;
    }
        /**
         * 匹配是否邀评关键词
         * @param input
         * @return boolean
         */
    public static boolean checkAppraise(String input) {

        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // 去除首尾空白字符
        String trimmedInput = input.trim();

        // 依次检查每个正则表达式
        for (Pattern pattern : APPRAISE_PATTERNS) {
            if (pattern.matcher(trimmedInput).find()) {
                return true;
            }
        }

        return false;
    }
        /**
         * 匹配是否邀评关键词
         * @param input
         * @return boolean
         */
    public static boolean checkNotAppraise(String input) {

        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // 去除首尾空白字符
        String trimmedInput = input.trim();

        // 依次检查每个正则表达式
        for (Pattern pattern : NOTAPPRAISE_PATTERNS) {
            if (pattern.matcher(trimmedInput).find()) {
                return true;
            }
        }

        return false;
    }
        /**
         * 匹配是否上门关键词
         * @param input
         * @return boolean
         */
    public static boolean checkIsHome(String input) {

        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // 去除首尾空白字符
        String trimmedInput = input.trim();

        // 依次检查每个正则表达式
        for (Pattern pattern : ISHOME_PATTERNS) {
            if (pattern.matcher(trimmedInput).find()) {
                return true;
            }
        }

        return false;
    }
        /**
         * 匹配是否上门关键词
         * @param input
         * @return boolean
         */
    public static boolean checkIsNotHome(String input) {

        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // 去除首尾空白字符
        String trimmedInput = input.trim();

        // 依次检查每个正则表达式
        for (Pattern pattern : NOTHOME_PATTERNS) {
            if (pattern.matcher(trimmedInput).find()) {
                return true;
            }
        }

        return false;
    }
        /**
         * 匹配是否有上门意向关键词
         * @param input
         * @return boolean
         */
    public static boolean checkIsHomeIdea(String input) {

        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // 去除首尾空白字符
        String trimmedInput = input.trim();

        // 依次检查每个正则表达式
        for (Pattern pattern : ISHOMEIDEA_PATTERNS) {
            if (pattern.matcher(trimmedInput).find()) {
                return true;
            }
        }

        return false;
    }
        /**
         * 匹配是否有上门意向关键词
         * @param input
         * @return boolean
         */
    public static boolean checkIsNotHomeIdea(String input) {

        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // 去除首尾空白字符
        String trimmedInput = input.trim();

        // 依次检查每个正则表达式
        for (Pattern pattern : NOTHOMEIDEA_PATTERNS) {
            if (pattern.matcher(trimmedInput).find()) {
                return true;
            }
        }

        return false;
    }
        /**
         * 匹配是否邀评关键词
         * @param input
         * @return boolean
         */
    public static boolean checkInviteComments(String input) {

        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // 去除首尾空白字符
        String trimmedInput = input.trim();

        // 依次检查每个正则表达式
        for (Pattern pattern : INVITECOMMENT_PATTERNS) {
            if (pattern.matcher(trimmedInput).find()) {
                return true;
            }
        }

        return false;
    }
        /**
         * 匹配是否邀评关键词
         * @param input
         * @return boolean
         */
    public static boolean checkNotInviteComments(String input) {

        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // 去除首尾空白字符
        String trimmedInput = input.trim();

        // 依次检查每个正则表达式
        for (Pattern pattern : NOTINVITECOMMENT_PATTERNS) {
            if (pattern.matcher(trimmedInput).find()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 质差回访上门否定
     * @param input
     * @return
     */
    public static boolean checkNOTHOMEQ7(String input) {

        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // 去除首尾空白字符
        String trimmedInput = input.trim();

        // 依次检查每个正则表达式
        for (Pattern pattern : NOTHOMEQ7_PATTERNS) {
            if (pattern.matcher(trimmedInput).find()) {
                return true;
            }
        }

        return false;
    }
    /**
     * 解析评分，返回1-10的整数，不匹配返回null
     * @param input
     * @return String
     */
    public static String parseScore(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        // 去除前后空格
        input = input.trim();

        // 正则表达式：匹配 1-10、一到十、1分-10分、一分到十分
        String regex = "^(10|[1-9]|十|[一二三四五六七八九])分?$";

        // 编译正则表达式
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.matches()) {
            String group = matcher.group(1);

            // 处理阿拉伯数字
            if (group.matches("[0-9]+")) {
                return group;
            }

            // 处理汉字数字
            switch (group) {
                case "一": return "1";
                case "二": return "2";
                case "三": return "3";
                case "四": return "4";
                case "五": return "5";
                case "六": return "6";
                case "七": return "7";
                case "八": return "8";
                case "九": return "9";
                case "十": return "10";
                default: return null;
            }
        }

        return null; // 不匹配任何格式
    }
}