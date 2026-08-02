# -*- coding: utf-8 -*-
"""图书馆演示数据一键灌：20 书目(真实封面上 MinIO) + 40 馆藏册 + 少量干净在途单/违约/信用流水。
运行: PYTHONUTF8=1 PYTHONIOENCODING=utf-8 python scripts/seed_demo_books.py
依赖: minio pymysql requests。图片经 OpenLibrary 下载(跟随302)→ MinIO(桶 ruoyi)→ cover_url 存 URL。
"""
import io, requests, pymysql
from datetime import datetime, timedelta
from minio import Minio

BUCKET, PUB = 'ruoyi', 'http://localhost:9000/ruoyi'
mc = Minio('localhost:9000', access_key='ruoyi', secret_key='ruoyi123', secure=False)
conn = pymysql.connect(host='localhost', port=3306, user='root', password='123456',
                       database='library_reservation', charset='utf8mb4', autocommit=False)
cur = conn.cursor()
VENUE, FLOOR3 = 2083909208096411650, 2083914024663191553

# (title, author, isbn, publisher, clc, callNo, cover, shelf, loc, price)
B = [
 ("深入理解计算机系统(原书第3版)","Randal E. Bryant 等著；龚奕利、贺莲译","9787111544937","机械工业出版社","TP303","TP303/B849",1,2,139.0),
 ("算法导论(原书第3版)","Thomas H. Cormen 等著；殷建平等译","9787111407010","机械工业出版社","TP301.6","TP301.6/K233",1,2,128.0),
 ("Python编程:从入门到实践(第2版)","Eric Matthes 著；袁国忠译","9787115546081","人民邮电出版社","TP311.561","TP311.561/M425",1,2,89.0),
 ("统计学习方法","李航","9787302275954","清华大学出版社","TP181","TP181/L327",1,2,38.0),
 ("数学分析(下册·第四版)","华东师范大学数学系 编","9787040295672","高等教育出版社","O17","O17/H971",2,2,42.0),
 ("时间简史(插图本)","史蒂芬·霍金 著；许明贤、吴忠超译","9787535732309","湖南科学技术出版社","P159-49","P159-49/H392",2,2,45.0),
 ("三体","刘慈欣","9787536692930","四川科学技术出版社","I247.5","I247.5/L592",3,3,23.0),
 ("活着","余华","9787506365437","作家出版社","I247.5","I247.5/Y906",3,3,20.0),
 ("平凡的世界(第三部)","路遥","9787530216781","北京十月文艺出版社","I247.57","I247.57/L845",3,3,39.0),
 ("红楼梦","(清)曹雪芹、高鹗 著","9787020002207","人民文学出版社","I242.4","I242.4/C231",3,3,59.0),
 ("围城","钱锺书","9787020024759","人民文学出版社","I246.5","I246.5/Q315",3,3,19.0),
 ("百年孤独","加西亚·马尔克斯 著；范晔译","9787544253994","南海出版公司","I775.45","I775.45/M337",3,3,39.5),
 ("人类简史:从动物到上帝","尤瓦尔·赫拉利 著；林俊宏译","9787508647357","中信出版社","K02","K02/H434",5,1,68.0),
 ("万历十五年","黄仁宇","9787108009821","生活·读书·新知三联书店","K248.305","K248.305/H434",5,1,32.0),
 ("理想国","(古希腊)柏拉图 著；郭斌和、张竹明译","9787100017565","商务印书馆","B502.232","B502.232/P745",6,1,29.0),
 ("苏菲的世界","乔斯坦·贾德 著；萧宝森译","9787506366380","作家出版社","B089","B089/J275",6,1,43.0),
 ("思考,快与慢","丹尼尔·卡尼曼 著；胡晓姣等译","9787508633558","中信出版社","B842.1","B842.1/K126",6,1,69.0),
 ("娱乐至死","尼尔·波兹曼 著；章艳译","9787508648286","中信出版社","G206.2","G206.2/B846",6,1,39.0),
 ("魔鬼经济学1","史蒂芬·列维特、都伯纳 著；王晓鹂译","9787508665931","中信出版社","F069.9","F069.9/L957",4,1,49.0),
 ("现代汉语词典(第7版)","中国社会科学院语言研究所词典编辑室 编","9787100124508","商务印书馆","H164","H164/Z667",6,1,109.0),
]

def upload_cover(isbn, url):
    r = requests.get(url, timeout=60, headers={'User-Agent': 'Mozilla/5.0'}, allow_redirects=True)
    r.raise_for_status()
    data = r.content
    if len(data) < 1200:
        raise Exception(f'封面过小({len(data)}B)疑似空白占位')
    key = f'covers/{isbn}.jpg'
    mc.put_object(BUCKET, key, io.BytesIO(data), length=len(data), content_type='image/jpeg')
    return f'{PUB}/{key}', len(data)

now = datetime.now()
print('== 1) 清理事务数据(测试期零散数据) ==')
for t in ['biz_reservation', 'biz_loan', 'biz_hold', 'biz_violation', 'biz_appeal', 'biz_blacklist', 'biz_credit_log']:
    cur.execute(f'DELETE FROM {t}')

print('== 2) 重灌 20 书目 + 40 馆藏册 + 封面上 MinIO ==')
cur.execute('DELETE FROM biz_book_item')
cur.execute('DELETE FROM biz_book')
item_id = 0
for i, (title, author, isbn, pub, clc, call, shelf, loc, price) in enumerate(B, start=1):
    cover, sz = upload_cover(isbn, f'https://covers.openlibrary.org/b/isbn/{isbn}-L.jpg')
    cur.execute("INSERT INTO biz_book (id,tenant_id,isbn,title,author,publisher,clc_no,call_no,cover_url,price,total_qty,avail_qty,status,create_dept,create_by,create_time,del_flag)"
                " VALUES (%s,'000000',%s,%s,%s,%s,%s,%s,%s,%s,2,2,1,103,1,%s,'0')",
                (i, isbn, title, author, pub, clc, call, cover, price, now))
    for c in (1, 2):
        item_id += 1
        cur.execute("INSERT INTO biz_book_item (id,tenant_id,book_id,barcode,call_no,location_id,shelf_id,status,create_dept,create_by,create_time,del_flag)"
                    " VALUES (%s,'000000',%s,%s,%s,%s,%s,1,103,1,%s,'0')",
                    (item_id, i, f'BC{item_id:05d}', call, loc, shelf, now))
    print(f'  {i:2} {title[:18]:<20} {clc:<11} 封面 {sz//1024}KB')

print('== 3) 读者信用重置 + 建档流水(保证不变式 credit_score=Σdelta) ==')
cur.execute("UPDATE biz_reader SET credit_score=100, perform_count=0, blacklist_flag=0, blacklist_end_time=NULL, status=0")
cur.execute("SELECT user_id FROM biz_reader ORDER BY user_id")
readers = [r[0] for r in cur.fetchall()]
clid = 0
def credit_log(rid, delta, rtype, desc, biz_type, biz_id, score_after):
    global clid; clid += 1
    cur.execute("INSERT INTO biz_credit_log (id,tenant_id,reader_id,delta,reason_type,reason_desc,biz_type,biz_id,score_after,create_dept,create_by,create_time)"
                " VALUES (%s,'000000',%s,%s,%s,%s,%s,%s,%s,103,1,%s)",
                (5000+clid, rid, delta, rtype, desc, biz_type, biz_id, score_after, now))
for rid in readers:
    credit_log(rid, 100, 1, '建档', 'reader', rid, 100)

print('== 4) 违约 + 扣分(信用分布/违约类型演示; 维持不变式) ==')
vid = 0
def violation(rid, vtype, deduct, desc):
    global vid; vid += 1
    cur.execute("INSERT INTO biz_violation (id,tenant_id,reader_id,violation_type,biz_type,deduct_score,occur_time,source,status,create_dept,create_by,create_time,del_flag)"
                " VALUES (%s,'000000',%s,%s,'demo',%s,%s,0,0,103,1,%s,'0')",
                (6000+vid, rid, vtype, deduct, now - timedelta(days=vid), now))
# reader -> [(违约类型, 扣分)]  (类型:1爽约2暂离3监督4未签退5逾期6预约架7遗失)
vios = {1006:[(1,10)], 1007:[(5,5),(2,5)], 1008:[(1,10),(4,5)], 1012:[(1,10),(5,5),(3,10)]}
for rid, vs in vios.items():
    score = 100
    for vtype, d in vs:
        violation(rid, vtype, d, '演示违约')
        score -= d
        credit_log(rid, -d, {1:2,2:3,3:4,4:5,5:6,6:7,7:8}[vtype], '违约扣分', 'violation', 6000+vid, score)
    cur.execute("UPDATE biz_reader SET credit_score=%s WHERE user_id=%s", (score, rid))

print('== 5) 在途预约(明天, 供平面图选座演示占用) ==')
tm = (now + timedelta(days=1)).strftime('%Y-%m-%d')
rid_ = 0
for reader, seat in [(1001,5),(1002,12),(1003,8)]:
    rid_ += 1
    cur.execute("INSERT INTO biz_reservation (id,tenant_id,reader_id,seat_id,area_id,floor_id,venue_id,reserve_date,start_time,end_time,source,status,away_count,create_dept,create_by,create_time,del_flag)"
                " VALUES (%s,'000000',%s,%s,1,%s,%s,%s,%s,%s,1,0,0,103,1,%s,'0')",
                (9100+rid_, reader, seat, FLOOR3, VENUE, tm, f'{tm} 14:00:00', f'{tm} 18:00:00', now))

print('== 6) 在途借阅(2正常+1逾期, 维护 avail_qty/册状态) ==')
def borrow(reader, book, item, days, overdue=False):
    due = now + timedelta(days=days)
    st, of = (2, 1) if overdue else (0, 0)
    cur.execute("INSERT INTO biz_loan (id,tenant_id,reader_id,item_id,book_id,borrow_time,due_time,renew_count,status,overdue_flag,recall_flag,create_dept,create_by,create_time,del_flag)"
                " VALUES (%s,'000000',%s,%s,%s,%s,%s,0,%s,%s,0,103,1,%s,'0')",
                (9200+item, reader, item, book, now - timedelta(days=5), due, st, of, now))
    cur.execute("UPDATE biz_book_item SET status=2 WHERE id=%s", (item,))
    cur.execute("UPDATE biz_book SET avail_qty=avail_qty-1 WHERE id=%s", (book,))
borrow(1003, 2, 3, 18)     # 王五 借 算法导论
borrow(1005, 7, 13, 18)    # 陈明 借 三体
borrow(1012, 10, 19, -3, overdue=True)  # 冯磊 红楼梦 逾期

conn.commit()

print('== 7) 校验 ==')
cur.execute("SELECT COUNT(*) FROM biz_book"); nb = cur.fetchone()[0]
cur.execute("SELECT COUNT(*) FROM biz_book_item"); ni = cur.fetchone()[0]
cur.execute("SELECT COUNT(*) FROM biz_book WHERE cover_url IS NULL OR cover_url=''"); nc = cur.fetchone()[0]
cur.execute("""SELECT COUNT(*) FROM biz_reader r WHERE r.credit_score <>
    (SELECT LEAST(GREATEST(IFNULL(SUM(delta),0),0),100) FROM biz_credit_log c WHERE c.reader_id=r.user_id)""")
bad = cur.fetchone()[0]
print(f'书目 {nb} 馆藏册 {ni} 无封面 {nc} 信用不变式不一致读者 {bad}')
conn.close()
print('完成。')
