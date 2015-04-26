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

#QQ的uid 用来放行QQ的14000和8080
QQUID=([qquid])

#全局代理IP
IP="127.0.0.1"

#代理端口
PORT="10080"

#DNS
DNSIP="114.114.114.114:53"

#链名
CName="yzq"


#开启转发功能!
echo "1"  > /proc/sys/net/ipv4/ip_forward

iptables -t nat -F
iptables -t mangle -F
iptables -t nat -X $CName
iptables -t nat -N $CName
iptables -t nat -I OUTPUT -o $IF_NAME -j $CName



#排除免流程序
iptables -t nat -A $CName -m owner --uid-owner $GID  -j ACCEPT

#排除DNS
iptables -t nat -A $CName   -p UDP --dport 53 -j ACCEPT
iptables -t nat -A $CName   -p TCP --dport 53 -j ACCEPT
#排除共享程序
iptables -t nat -A $CName  -p UDP --dport 67:68 -j ACCEPT

#排除QQ(手机QQ  国际QQ QQ轻聊版)
for qq in ${QQUID[@]};
do
if [ $qq ];then
iptables -t nat -A $CName -p tcp -m owner --uid-owner $qq --dport 14000 -j ACCEPT
iptables -t nat -A $CName -p tcp -m owner --uid-owner $qq --dport 8080 -j ACCEPT
fi
done

#共享上网
for sif in ${S_IF[@]};
do
if [ $sif ];then
iptables -t nat -A PREROUTING -i $sif  -p UDP --dport 53 -j ACCEPT
iptables -t nat -A PREROUTING -i $sif  -s 192.168.43.0/24 -p TCP -j REDIRECT --to-ports  $PORT
iptables -t nat -A PREROUTING -i $sif  -s 192.168.43.0/24 -p UDP -j REDIRECT --to-ports  $PORT
iptables -t nat -A PREROUTING -i $sif  -s 192.168.43.0/24 -p SCTP -j REDIRECT --to-ports  $PORT

iptables -t nat -A PREROUTING -i $sif  -s 192.168.42.0/24 -p TCP -j REDIRECT --to-ports  $PORT
iptables -t nat -A PREROUTING -i $sif  -s 192.168.42.0/24 -p UDP -j REDIRECT --to-ports  $PORT
iptables -t nat -A PREROUTING -i $sif  -s 192.168.42.0/24 -p SCTP -j REDIRECT --to-ports  $PORT
fi
done

iptables -t nat -A POSTROUTING -s 192.168.43.0/24 -o $IF_NAME -j MASQUERADE
iptables -t nat -A POSTROUTING -s 192.168.42.0/24 -o $IF_NAME -j MASQUERADE

#不免排除
for uid2 in ${UID2[@]};
do
if [ $uid2 ];then
iptables -t nat -A $CName -m owner --uid-owner $uid2 -j ACCEPT
fi
done

#排除HTTPS协议
[isHttps]iptables -t nat -A $CName -p TCP --dport 443 -j ACCEPT
#重定向 80 8080
iptables -t nat -A $CName -p TCP --dport 80 -j  DNAT --to-destination  $IP:$PORT
iptables -t nat -A $CName -p TCP --dport 8080 -j  DNAT --to-destination  $IP:$PORT


#半免UID
for uid in ${UID[@]};
do
if [ $uid ];then
iptables -t nat -A $CName -m owner --uid-owner $uid -j ACCEPT
fi
done


#其他 即全免
iptables -t nat -A $CName -p UDP -j DNAT --to-destination $IP:$PORT
iptables -t nat -A $CName -p TCP -j DNAT --to-destination $IP:$PORT
iptables -t nat -A $CName -p SCTP -j DNAT --to-destination $IP:$PORT
