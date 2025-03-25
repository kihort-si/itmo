# Инициируем репозиторий
svnadmin create repo
cd repo
svn mkdir -m "project structure" file:///home/studs/s366389/opi/svn/repo/trunk file:///home/studs/s366389/opi/svn/repo/branches
cd ..
svn checkout file://$(pwd)/repo/trunk/ wc
cd wc

# Коммит (r0)
cp -r /home/studs/s366389/opi/commits/commit0/* .
svn add --force .
svn commit -m "commit0" --username=red

# Создаем новую ветку b1 и делаем в ней коммит (r1)
svn copy file:///home/studs/s366389/opi/svn/repo/trunk file:///home/studs/s366389/opi/svn/repo/branches/b1 -m "Create branch b1"
svn switch file:///home/studs/s366389/opi/svn/repo/branches/b1
cp -r /home/studs/s366389/opi/commits/commit1/* .
svn add --force .
svn commit -m "commit1" --username=blue

# Создаем новую ветку b2 и делаем в ней коммит (r2)
svn copy file:///home/studs/s366389/opi/svn/repo/branches/b1 file:///home/studs/s366389/opi/svn/repo/branches/b2 -m "Create branch b2"
svn switch file:///home/studs/s366389/opi/svn/repo/branches/b2
cp -r /home/studs/s366389/opi/commits/commit2/* .
svn add --force .
svn commit -m "commit2" --username=blue

# Переключаемся на ветку trunk и делаем в ней коммит (r3)
svn switch file:///home/studs/s366389/opi/svn/repo/trunk
cp -r /home/studs/s366389/opi/commits/commit3/* .
svn add --force .
svn commit -m "commit3" --username=red

# Создаем новую ветку b3 и делаем в ней коммит (r4)
svn copy file:///home/studs/s366389/opi/svn/repo/trunk file:///home/studs/s366389/opi/svn/repo/branches/b3 -m "Create branch b3"
svn switch file:///home/studs/s366389/opi/svn/repo/branches/b3
cp -r /home/studs/s366389/opi/commits/commit4/* .
svn add --force .
svn commit -m "commit4" --username=blue

# Переключаемся на ветку trunk и делаем в ней коммит (r5)
svn switch file:///home/studs/s366389/opi/svn/repo/trunk
cp -r /home/studs/s366389/opi/commits/commit5/* .
svn add --force .
svn commit -m "commit5" --username=red

# Делаем коммит (r6)
cp -r /home/studs/s366389/opi/commits/commit6/* .
svn add --force .
svn commit -m "commit6" --username=red

# Переключаемся на ветку b2 и делаем в ней коммит (r7)
svn switch file:///home/studs/s366389/opi/svn/repo/branches/b2
cp -r /home/studs/s366389/opi/commits/commit7/* .
svn add --force .
svn commit -m "commit7" --username=blue

# Делаем коммит (r8)
cp -r /home/studs/s366389/opi/commits/commit8/* .
svn add --force .
svn commit -m "commit8" --username=blue

# Делаем слияние ветки b2 в b3
svn update
svn switch file:///home/studs/s366389/opi/svn/repo/branches/b3
svn merge file:///home/studs/s366389/opi/svn/repo/branches/b2
svn status

# Делаем коммит (r9)
cp -r /home/studs/s366389/opi/commits/commit9/* .
svn add --force .
svn commit -m "commit9" --username=blue

# Переключаемся на ветку b1 и делаем в ней коммит (r10)
svn switch file:///home/studs/s366389/opi/svn/repo/branches/b1
cp -r /home/studs/s366389/opi/commits/commit10/* .
svn add --force .
svn commit -m "commit10" --username=blue

# Переключаемся на ветку trunk и делаем в ней коммит (r11)
svn switch file:///home/studs/s366389/opi/svn/repo/trunk
cp -r /home/studs/s366389/opi/commits/commit11/* .
svn add --force .
svn commit -m "commit11" --username=red

# Переключаемся на ветку b1 и делаем в ней коммит (r12)
svn switch file:///home/studs/s366389/opi/svn/repo/branches/b1
cp -r /home/studs/s366389/opi/commits/commit12/* .
svn add --force .
svn commit -m "commit12" --username=blue

# Переключаемся на ветку b3 и делаем в ней коммит (r13)
svn switch file:///home/studs/s366389/opi/svn/repo/branches/b3
cp -r /home/studs/s366389/opi/commits/commit13/* .
svn add --force .
svn commit -m "commit13" --username=blue

# Переключаемся на ветку trunk и делаем в ней коммит (r14)
svn switch file:///home/studs/s366389/opi/svn/repo/trunk
cp -r /home/studs/s366389/opi/commits/commit14/* .
svn add --force .
svn commit -m "commit14" --username=red

# Переключаемся на ветку b1 и делаем в ней коммит (r15)
svn switch file:///home/studs/s366389/opi/svn/repo/branches/b1
cp -r /home/studs/s366389/opi/commits/commit15/* .
svn add --force .
svn commit -m "commit15" --username=blue

# Делаем коммит (r16)
cp -r /home/studs/s366389/opi/commits/commit16/* .
svn add --force .
svn commit -m "commit16" --username=blue

# Переключаемся на ветку b3 и делаем в ней коммит (r17)
svn switch file:///home/studs/s366389/opi/svn/repo/branches/b3
cp -r /home/studs/s366389/opi/commits/commit17/* .
svn add --force .
svn commit -m "commit17" --username=blue

# Переключаемся на ветку b1 и делаем в ней коммит (r18)
svn switch file:///home/studs/s366389/opi/svn/repo/branches/b1
cp -r /home/studs/s366389/opi/commits/commit18/* .
svn add --force .
svn commit -m "commit18" --username=blue

# Делаем коммит (r19)
cp -r /home/studs/s366389/opi/commits/commit19/* .
svn add --force .
svn commit -m "commit19" --username=blue

# Делаем слияние ветки b1 в b3
svn update
svn switch file:///home/studs/s366389/opi/svn/repo/branches/b3
svn merge file:///home/studs/s366389/opi/svn/repo/branches/b1
svn status
svn resolve --accept=working /home/studs/s366389/opi/svn/wc/*

# Делаем коммит (r20)
cp -r /home/studs/s366389/opi/commits/commit20/* .
svn add --force .
svn commit -m "commit20" --username=blue

# Делаем слияние ветки b3 в trunk
svn update
svn switch file:///home/studs/s366389/opi/svn/repo/trunk
svn merge file:///home/studs/s366389/opi/svn/repo/branches/b3
svn status
svn resolve --accept=working /home/studs/s366389/opi/svn/wc/*

# Делаем коммит (r21)
cp -r /home/studs/s366389/opi/commits/commit21/* .
svn add --force .
svn commit -m "commit21" --username=red
