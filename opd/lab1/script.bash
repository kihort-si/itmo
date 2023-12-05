#task 1

mkdir arbok3
echo "satk=10 sdef=9 spd=5" > arbok3/vileplume
mkdir arbok3/mienshao
mkdir arbok3/bidoof
mkdir arbok3/yanma
mkdir arbok3/natu
mkdir arbok3/shellos
mkdir minccino3
echo -e "Способности Mountain Peak Swarm Battle Armor\nRock Head" > minccino3/armaldo
echo "Тип диеты Herbivore" > minccino3/chimecho
echo -e "Способности Tackle\nHarden Block Rock Throw Thunder Wave Rock Blast Rest Spark Rock Slide\nPower Gem Sandstorm Discharge Earth Power Stone Edge Lock-On Zap\nCannon" > minccino3/nosepass
mkdir minccino3/braviary
mkdir minccino3/musharna
mkdir minccino3/scrafty
mkdir vullaby8
mkdir vullaby8/vanilluxe
mkdir vullaby8/shedinja
mkdir vullaby8/tyranitar
echo -e "Способности Venom Sticky Hold Liquid\nOoze" > vullaby8/gulpin
echo -e "Возможности Overland=1 Surface=7 Underwater=7 Jump=2\nPower=1 Intelligence=4 Fountain=0 Invisibility=0" > vullaby8/frillish
echo -e "Тип диеты\nPhototroph Herbivore" > vullaby8/chikorita
echo "Тип диеты Carnivore" > yanmega9
echo -e "Живет Desert\nMountain" > flygon6
echo -e "Возможности Overland=8 Surface=6 Burrow=8\nJump=3 Power=4 Intelligence=4 Stealth=0\nGroundshaper=0" > krookodile6

#task 2

chmod 335 arbok3
chmod u=r arbok3/vileplume
chmod g=r arbok3/vileplume
chmod o-rwx arbok3/vileplume
chmod u=rx arbok3/mienshao
chmod g=rwx arbok3/mienshao
chmod o=rw arbok3/mienshao
chmod u=rx arbok3/bidoof
chmod g=x arbok3/bidoof
chmod o=x arbok3/bidoof
chmod 731 arbok3/yanma
chmod 312 arbok3/natu
chmod 750 arbok3/shellos
chmod u=rw flygon6
chmod g=w flygon6
chmod o=r flygon6
chmod 404 krookodile6
chmod u=wx minccino3
chmod g=wx minccino3
chmod o=rx minccino3
chmod 044 minccino3/armaldo
chmod 511 minccino3/chimecho
chmod 101 minccino3/nosepass
chmod a=rwx minccino3/braviary
chmod 361 minccino3/musharna
chmod 561 minccino3/scrafty
chmod a=rwx vullaby8
chmod 375 vullaby8/vanilluxe
chmod 404 vullaby8/gulpin
chmod 044 vullaby8/frillish
chmod 513 vullaby8/shedinja
chmod 660 vullaby8/chikorita
chmod 311 vullaby8/tyranitar
chmod 404 yanmega9

#task 3

#Изменим права для работы

chmod u=rwx yanmega9
chmod u=rwx arbok3
chmod u=rwx arbok3/natu
chmod u=rwx arbok3/bidoof


cat vullaby8/chikorita vullaby8/gulpin > krookodile6_35
ln yanmega9 minccino3/armaldoyanmega 
ln -s ../krookodile6 ./minccino3/armaldokrookodile 
cat yanmega9 > minccino3/armaldoyanmega
cp -r arbok3 vullaby8/vanilluxe
cp yanmega9 arbok3/bidoof
ln -s vullaby8 Copy_87

#task 4

#Изменим права для работы

chmod u=rwx krookodile6

wc -l minccino3/nosepass vullaby8/gulpin vullaby8/frillish 2>/tmp/mer | grep -v total | sort -r
ls -Rla 2>/dev/null | grep -Ei ':[0-9][0-9] \S*ko.*\b' | sort -k7Mr -k6nr -k8r | tail -n 2
cat -n `ls -R 2>/dev/null | grep -E ^k` 2>/dev/null | sort -k2 -r
ls -Rula 2>/tmp/merrr | grep -Ei ':[0-9][0-9] \S*chi.*\b' | sort -k7M -k6n -k8 | head -n 2
wc -l < krookodile6 | cat >> krookodile6
grep Mo flygon6 2>/dev/null

#task 5

#Изменим права для работы

chmod 777 vullaby8/tyranitar

rm -f krookodile6
rm -f minccino3/armaldo
rm -f Copy_*
rm -f minccino3/armaldoyanme*
rm -rf vullaby8
rmdir arbok3/mienshao