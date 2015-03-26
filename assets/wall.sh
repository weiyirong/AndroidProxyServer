﻿#!/system/bin/sh

#网卡名称
IF_NAME="[gprs_interface]"

#WIFI和usb共享网卡
S_IF=([shared_interface])

#半免UID
UID=([banmian])

#完全排除，目前我完全排除的只有支付宝，或许银行的app也要完全排除
UID2=([bumian])

#将10081修改为你的almp的UID，其它软件用改为0
GID="[uid]"

#默认启用HTTPS，登陆用到，关闭(HTTPS="#")即可,注意双引号中有个空格，否则不会生效
#特别注意115网盘5.0是走HTTPS的，如果想免流量使用，请使用旧版4.0，
#其他常用app一般走HTTPS流量比较少，基本可以忽略不计

HTTPS=" "

#全局代理IP
IP="127.0.0.1"

#代理端口
PORT="10080"

#DNS
DNSIP="114.114.114.114"


#链名
CName="sdswall"

#################################
#此部分切勿修改，否则后果自负！

iptables -t nat -F
iptables -t mangle -F
iptables -t nat -N $CName
iptables -t nat -I OUTPUT -o $IF_NAME -j $CName 


#防跳,新旧iptables写法不同，都写了。
iptables -t mangle -I PREROUTING -i $IF_NAME ! -p icmp -m state --state RELATED,INVALID -j DROP
iptables -t mangle -I PREROUTING -i $IF_NAME -p ! icmp -m state --state RELATED,INVALID -j DROP


iptables -t nat -I $CName -p udp -j DNAT --to-destination $IP:$PORT
iptables -t nat -I $CName -p tcp -j DNAT --to-destination $IP:$PORT
iptables -t nat -I $CName -p sctp -j DNAT --to-destination $IP:$PORT


#排除HTTPS协议，默认启用
iptables -t nat -I $CName -p tcp --dport 443 -j ACCEPT


###########

#半免UID

for uid in ${UID[@]};
do
if [ $uid ];then
iptables -t nat -I $CName -m owner --uid-owner $uid -j ACCEPT
fi
done

############
#重定向HTTP
iptables -t nat -I $CName -p tcp --dport 80 -j  DNAT --to-destination  $IP:$PORT

#排除免流程序
iptables -t nat -I $CName -p tcp -m owner --uid-owner $GID  -j ACCEPT

#不免
for uid2 in ${UID2[@]};
do
if [ $uid2 ];then
iptables -t nat -I $CName -p tcp -m owner --uid-owner $uid2 -j ACCEPT
fi
done

#share
iptables -t nat -I POSTROUTING -o $IF_NAME -s 192.168.43.0/24 -j MASQUERADE
iptables -t nat -I POSTROUTING -o $IF_NAME -s 192.168.42.0/24 -j MASQUERADE

for ifname in ${S_IF[@]};
do
if [ $ifname ];then
iptables -t nat -I PREROUTING -i $ifname -s 192.168.43.0/24 -p tcp --dport 80 -j  REDIRECT --to-ports  $PORT
iptables -t nat -I PREROUTING -i $ifname -s 192.168.42.0/24 -p tcp --dport 80 -j  REDIRECT --to-ports  $PORT
fi
done

iptables -t nat -I $CName -p tcp --dport 53 -j  ACCEPT


#PS:安安修改，原创作者：扫地僧5.0
#iptables -I INPUT ! -p icmp -m string --string "广告" --algo bm -j DROP

#必须开启转发功能!
echo "1"  > /proc/sys/net/ipv4/ip_forward 

#非samp把GID换成0，将代理换成80即可

