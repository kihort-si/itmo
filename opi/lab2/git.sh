# Инициируем репозиторий
git init

# Выбираем красного пользователя
git config user.name "red"
git config user.email "red@domain.com"

# Коммит (r0)
cp -r /home/studs/s366389/opi/commits/commit0/* .
git add .
git commit -m "commit0"

# Меняем пользователя на синего
git config user.name "blue"
git config user.email "blue@domain.com"

# Создаем новую ветку b1 и делаем в ней коммит (r1)
git checkout -b "b1"
cp -r /home/studs/s366389/opi/commits/commit1/* .
git add .
git commit -m "commit1"

# Создаем новую ветку b2 и делаем в ней коммит (r2)
git checkout -b "b2"
cp -r /home/studs/s366389/opi/commits/commit2/* .
git add .
git commit -m "commit2"

# Меняем пользователя на красного
git config user.name "red"
git config user.email "red@domain.com"

# Переключаемся на ветку master и делаем в ней коммит (r3)
git checkout "master"
cp -r /home/studs/s366389/opi/commits/commit3/* .
git add .
git commit -m "commit3"

# Меняем пользователя на синего
git config user.name "blue"
git config user.email "blue@domain.com"

# Создаем новую ветку b3 и делаем в ней коммит (r4)
git checkout -b "b3"
cp -r /home/studs/s366389/opi/commits/commit4/* .
git add .
git commit -m "commit4"

# Меняем пользователя на красного
git config user.name "red"
git config user.email "red@domain.com"

# Переключаемся на ветку master и делаем в ней коммит (r5)
git checkout "master"
cp -r /home/studs/s366389/opi/commits/commit5/* .
git add .
git commit -m "commit5"

# Делаем коммит (r6)
cp -r /home/studs/s366389/opi/commits/commit6/* .
git add .
git commit -m "commit6"

# Меняем пользователя на синего
git config user.name "blue"
git config user.email "blue@domain.com"

# Переключаемся на ветку b2 и делаем в ней коммит (r7)
git checkout "b2"
cp -r /home/studs/s366389/opi/commits/commit7/* .
git add .
git commit -m "commit7"

# Делаем коммит (r8)
cp -r /home/studs/s366389/opi/commits/commit8/* .
git add .
git commit -m "commit8"

# Делаем слияние ветки b2 в b3
git checkout "b3"
git merge "b2"

# Делаем коммит (r9)
cp -r /home/studs/s366389/opi/commits/commit9/* .
git add .
git commit -m "commit9"

# Переключаемся на ветку b1 и делаем в ней коммит (r10)
git checkout "b1"
cp -r /home/studs/s366389/opi/commits/commit10/* .
git add .
git commit -m "commit10"

# Меняем пользователя на красного
git config user.name "red"
git config user.email "red@domain.com"

# Переключаемся на ветку master и делаем в ней коммит (r11)
git checkout "master"
cp -r /home/studs/s366389/opi/commits/commit11/* .
git add .
git commit -m "commit11"

# Меняем пользователя на синего
git config user.name "blue"
git config user.email "blue@domain.com"

# Переключаемся на ветку b1 и делаем в ней коммит (r12)
git checkout "b1"
cp -r /home/studs/s366389/opi/commits/commit12/* .
git add .
git commit -m "commit12"

# Переключаемся на ветку b3 и делаем в ней коммит (r13)
git checkout "b3"
cp -r /home/studs/s366389/opi/commits/commit13/* .
git add .
git commit -m "commit13"

# Меняем пользователя на красного
git config user.name "red"
git config user.email "red@domain.com"

# Переключаемся на ветку master и делаем в ней коммит (r14)
git checkout "master"
cp -r /home/studs/s366389/opi/commits/commit14/* .
git add .
git commit -m "commit14"

# Меняем пользователя на синего
git config user.name "blue"
git config user.email "blue@domain.com"

# Переключаемся на ветку b1 и делаем в ней коммит (r15)
git checkout "b1"
cp -r /home/studs/s366389/opi/commits/commit15/* .
git add .
git commit -m "commit15"

# Делаем коммит (r16)
cp -r /home/studs/s366389/opi/commits/commit16/* .
git add .
git commit -m "commit16"

# Переключаемся на ветку b3 и делаем в ней коммит (r17)
git checkout "b3"
cp -r /home/studs/s366389/opi/commits/commit17/* .
git add .
git commit -m "commit17"

# Переключаемся на ветку b1 и делаем в ней коммит (r18)
git checkout "b1"
cp -r /home/studs/s366389/opi/commits/commit18/* .
git add .
git commit -m "commit18"

# Делаем коммит (r19)
cp -r /home/studs/s366389/opi/commits/commit19/* .
git add .
git commit -m "commit19"

# Делаем слияние ветки b1 в b3
git checkout "b3"
git merge "b1"

# Выбираем нужную версию для текстовых файлов, для бинарного используем checkout --theirs
git checkout --theirs _
git add C.java F.java I.java _
git commit

# Делаем коммит (r20)
cp -r /home/studs/s366389/opi/commits/commit20/* .
git add .
git commit -m "commit20"
2
# Меняем пользователя на красного
git config user.name "red"
git config user.email "red@domain.com"

# Делаем слияние ветки b3 в master
git checkout "master"
git merge "b3"

# Выбираем нужную версию для текстовых файлов, для бинарного используем checkout --theirs
git checkout --theirs _
git add C.java F.java I.java _
git commit

# Делаем коммит (r21)
cp -r /home/studs/s366389/opi/commits/commit21/* .
git add .
git commit -m "commit21"
