#!/system/bin/sh
#扫地僧防跳脚本4.0.1专业版

##########
##排除设置
#半免排除UID: （在下面引号中添加应用的uid，必须按照"10001 10002 10003"的格式）
BANMIAN="[banmian]"

#不免排除UID: （在下面引号中添加应用的uid，必须按照"10001 10002 10003"的格式）
BUMIAN="[bumian]"



##########
##参数设置
#网卡: （缺少的网卡自行添加，多余的网卡可以删除）
IF="[gprs_interface]"

#全局代理IP:
IP="127.0.0.1"
#全局代理端口:
PORT="10080"

#全局代理UID: （almp和anmpp默认为0，其它全局软件请改为对应的UID）
ROOT="[uid]"

#HTTPS放行:
HTTPS=""


#DNS开关：
DNS=""

#DNS IP:
DNSIP="114.114.115.115"






##以下所有代码请勿修改

##########
##防跳规则
iptables -t nat -F
iptables -t nat -N sdswall
for sdsif in $IF
do
if [ $sdsif != "" ]
then
iptables -t nat -I OUTPUT -o $sdsif -j sdswall
fi
done
iptables -t nat -I sdswall -p 132 -j DNAT --to-destination $IP:$PORT
iptables -t nat -I sdswall -p 17 -j DNAT --to-destination $IP:$PORT
iptables -t nat -I sdswall -p 6 -j DNAT --to-destination $IP:$PORT


#########
#排除规则
#root
iptables -t nat -I sdswall -p 6 -m owner --uid-owner $ROOT -j ACCEPT
#HTTPS
$HTTPS iptables -t nat -I sdswall -p 6 --dport 443 -j ACCEPT
#DNS
#$DNS 
iptables -t nat -I sdswall -p 17 --dport 53 -j ACCEPT 
#--to-destination $DNSIP:53
#通用半免
for sdsuid in $BANMIAN
do
if [ $sdsuid != "" ]
then
iptables -t nat -I sdswall -m owner --uid-owner $sdsuid -j ACCEPT
iptables -t nat -I sdswall -m owner --uid-owner $sdsuid -p 6 --dport 80 -j DNAT --to-destination $IP:$PORT
fi
done
#通用不免
for sds2uid in $BUMIAN
do
if [ $sds2uid != "" ]
then
iptables -t nat -I sdswall -m owner --uid-owner $sds2uid -j ACCEPT
fi
done

