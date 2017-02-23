package lbs;

/**
 * Created by kail on 2017/2/23.
 */
public class LLUtil {

    private static final Float EARTH_RADIUS = 6378.137F;

    private static double rad(double f) {
        return f * Math.PI / 180.0;
    }

    /**
     * @param lat1 纬度
     * @param lng1 经度
     */
    private static double getDistance(double lat1, double lng1, double lat2, double lng2) {

        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;

        return Math.round(s * 10000D) / 10000D;
    }

    /**
     * 智慧广场 121.437236,31.240008
     * 丰庄 121.369699,31.255901
     */
    public static void main(String[] args) {
        System.out.println(getDistance(121.437236, 31.240008, 121.369699, 31.255901)); // 智慧广场到丰庄茶城 7.5745， 与百度地图差200米
        System.out.println(getDistance(121.444592,31.246432, 121.351025,31.161678)); // 智慧广场到七宝 11.5174， 与百度地图差500米
    }

}
