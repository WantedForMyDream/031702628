import net.sf.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static  void main(String[] args) throws Exception {
//        args = new String[]{"1.txt","2.txt"};
//        Scanner scanner=new Scanner(System.in);
//        String string =scanner.nextLine();
//        String string="1!钭隆箍,山西省阳泉市矿13100299356区赛鱼街道麻地巷32号楼.";

//        resolution(string);
        List<String> list= Utils.fileRead(args[0]);
        String str="";
        for(int i=0;i<list.size();i++){
//            System.out.println(list.get(i));
            str=str+resolution(list.get(i))+"\n";
        }
        Utils.fileWrite(args[1],str);
    }

    public static String resolution(String string){
        int flag=-1;
        String str=string.substring(0,1);
        if (str.equals("1")){
            flag=1;
        }else if(str.equals("2")){
            flag=2;
        }else{
            flag=3;
        }
        string=string.substring(2,string.length());
//        System.out.println(string);

        Map map=new HashMap();
        map=nameAndNumberResolution(string);
        String name=null;
        String number=null;
        String address=null;
        if(map.containsKey("姓名"))
            name=map.get("姓名").toString();
        if(map.containsKey("手机"))
            number=map.get("手机").toString();
        if(map.containsKey("地址"))
            address=map.get("地址").toString();
//        System.out.println(name+" "+number+" "+address);

        map.remove("地址");
        map.put("地址",addressResolution(flag,address));

        JSONObject jsonObject=JSONObject.fromObject(map);
        String json=jsonObject.toString();
//        System.out.println(json);
        return json;
    }

    //从字符串中分离出姓名和手机号码，剩下地址
    //调用了numberResolution()方法
    public static Map nameAndNumberResolution(String string){
        Map map=new HashMap();
        String s=null;
        Pattern pattern = Pattern.compile("[,]");
        String[] str=pattern.split(string);
        for(int i=0;i<str.length;i++){
            if(i==0){
                map.put("姓名",str[i]);
            }
            else {
                s=numberResolution(str[i]);
                if(!s.equals(null)){
                    map.put("手机",s);
                    s=str[i].replace(s,"");
                    map.put("地址",s);
                }
            }
        }
        return map;
    }

    public static String numberResolution(String string){
        String str=null;
        Pattern pattern = Pattern.compile("1[0-9]{10}");
        Matcher matcher =pattern.matcher(string);
        if(matcher.find())
        {
            str=matcher.group();
        }
        return str;
    }

    public static ArrayList addressResolution(int flag, String add){
        ArrayList list=new ArrayList();
        list=getAdd(flag,add);
        return list;
    }


    public static Map getLngAndLat(String address){
        Map map=new HashMap();

        Pattern pattern = Pattern.compile("([0-9]*#[0-9]*)");//先把10#111这些字符剔除
        Matcher matcher=pattern.matcher(address);
        if(matcher.find()){
            address=address.replace(matcher.group(),"");
        }
//        System.out.println(address);

        String url = "http://api.map.baidu.com/geocoder/v2/?address="+address+"&output=json&ak=859d16285fd000feec89e9032513f8bb";
        String json = Utils.loadJSON(url);
        JSONObject obj = JSONObject.fromObject(json);
        if(obj.get("status").toString().equals("0")){
            double lng=obj.getJSONObject("result").getJSONObject("location").getDouble("lng");
            double lat=obj.getJSONObject("result").getJSONObject("location").getDouble("lat");
            map.put("lng", Utils.getDecimal(lng));
            map.put("lat", Utils.getDecimal(lat));
//            System.out.println("经度："+map.get("lng")+"---纬度："+map.get("lat"));
        }else{
            System.out.println("未找到相匹配的经纬度！");
        }
        return map;
    }



    public static ArrayList getAdd(int flag,String address){
        Map map=getLngAndLat(address);
        double lat=0;
        double lng=0;
        if(map.containsKey("lat"))
            lat=(double)map.get("lat");//纬度
        if(map.containsKey("lng"))
            lng=(double)map.get("lng");//经度
        String url="http://api.map.baidu.com/geocoder/v2/?callback=renderReverse&location="+lat+","+lng+"&output=json&pois=1&ak=859d16285fd000feec89e9032513f8bb";
        String string = Utils.loadJSON(url);
        string=string.substring(29,string.length()-1);//去除renderReverse&&renderReverse()
        JSONObject obj = JSONObject.fromObject(string);

        ArrayList list=new ArrayList();
        String province=null;
        String city=null;
        String district=null;
        String town=null;
        String street=null;
        String street_number=null;

        if(obj.get("status").toString().equals("0")){

            province=obj.getJSONObject("result").getJSONObject("addressComponent").getString("province");
            province=province.replace("市","");
            list.add(province);
//            System.out.println(province);

            city=obj.getJSONObject("result").getJSONObject("addressComponent").getString("city");
            list.add(city);
//            System.out.println(city);

            district="";
            if(!city.equals("东莞市")&&!city.equals("中山市")&&!city.equals("三沙市")&&!city.equals("儋州市")&&!city.equals("嘉峪关市"))
            {
                district=obj.getJSONObject("result").getJSONObject("addressComponent").getString("district");
            }
            list.add(district);
//            System.out.println(district);
            town=obj.getJSONObject("result").getJSONObject("addressComponent").getString("town");
            street=obj.getJSONObject("result").getJSONObject("addressComponent").getString("street");
            street_number=obj.getJSONObject("result").getJSONObject("addressComponent").getString("street_number");
        }

        if(address.indexOf(city)>=0&&address.indexOf(city)<=9){
            address=address.substring(address.indexOf(city)+city.length(),address.length());
//            System.out.println(address);
        }else{
            city=city.substring(0,3);
            if(address.indexOf(city)>=0){
                address=address.substring(address.indexOf(city)+city.length(),address.length());
            }
            else{
                city=city.substring(0,2);
                if(address.indexOf(city)>=0){
                    address=address.substring(address.indexOf(city)+city.length(),address.length());
                }
            }
        }
        if(!district.equals("")&&address.indexOf(district)>=0&&address.indexOf(district)<=4){
            address=address.substring(address.indexOf(district)+district.length(),address.length());
        }

        Pattern pattern = Pattern.compile("(.+?镇|.+?街道|.+?乡|.+?民族乡|.+?县辖区)[.+街|.+巷|.+路|.+胡同|.+道|.+弄|.+坊|.+里|.+市]*");
        town="";
        Matcher matcher =pattern.matcher(address);
        if(matcher.find())
        {
            town=matcher.group(1);
        }
        list.add(town);
//        System.out.println(town);

        String fullAdd="";
        fullAdd=address.substring(town.length(),address.length()).replace(".","");
        if(flag==1){
            list.add(fullAdd);
            return list;
        }


        if(!street.equals(null)){
            Pattern pattern1 = Pattern.compile("(.+街|.+巷|.+路|.+胡同|.+道|.+弄|.+坊|.+里|.+市)([1-9][0-9]*号)");
            Matcher matcher1 =pattern1.matcher(fullAdd);
            if(matcher1.find())
            {
                street=matcher1.group(1);
                street_number=matcher1.group(2);
            }
//            System.out.println(street+" "+street_number);
        }
        list.add(street);
        list.add(street_number);
        fullAdd=fullAdd.replace(street,"").replace(street_number,"");
        list.add(fullAdd);
        return list;
    }
}
