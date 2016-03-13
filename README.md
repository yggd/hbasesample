# IDE(Eclipse, IntelliJ Idea)でApache Spark(Java)を使って仮想環境上のHBaseと疎通する。

## 前提
1. HBaseの動作環境はDockerイメージ(cloudera/quickstart)を使用
2. HBase内にテーブルを作り、クライアント環境のIDEから疎通を試みる。
3. クライアント(IDE)環境はMac/Windowsを想定
4. Dockerのダウンロード時間にもよるが、作業時間は30分程度を想定。（つまり、疎通までを目的として、細かい説明は省略している）

## 注意
1. そこそこ性能がよく、メモリが多く(8GB)積まれているPCを用意すること。

### Dockerのインストール
ローカル環境にまだ入れてなければ、[ここ](https://www.docker.com)からDockerを入手し、インストールする。
(Windows/Macの場合は コンテナの動作環境としてVirtual Boxも一緒にインストールされる 。)

### 初期設定とDockerイメージの入手

(Dockerの初回起動手順)

1. Dockerのターミナルを起動する。
アプリケーション>Docker>Docker Quickstart Terminal
(Windowsの場合はメニュー>プログラム>Docker>Docker Quickstart Terminal)から
初回起動時はVirtual Boxの実行環境から構築するため、やや時間がかかる。
2. (以下は初回起動時のみ）起動し終わってクジラの絵(AA)が出てきたら、動作するコンテナのIPアドレスを控えておく。
3. デフォルトではDockerコンテナ実行環境が貧弱なため、一旦exitで抜ける。
4. Virtual Boxマネージャーを起動し、Dockerコンテナ実行環境も`shutdown -h now`で停止させる。
5. メニューの「設定」で、CPUのコア数(2くらい)、メインメモリ(4GBくらい)に増やして「OK」を押す。
6. /etc/hosts(Windowsはc:¥windows¥system32¥drivers¥etc)に以下ホストと2.で得られたIPアドレスを対応づける。

```
192.168.99.100  quickstart.cloudera
```
IPアドレスは2.で得られたものを指定する。

(2回目以降のDocker起動手順)

1. Dockerのターミナルを起動する。
アプリケーション>Docker>Docker Quickstart Terminal
(Windowsの場合はメニュー>プログラム>Docker>Docker Quickstart Terminal)から
初回起動時はVirtual Boxの実行環境から構築するため、やや時間がかかる。
2. 起動し終わってクジラの絵(AA)が出てきたら、HBaseを実行するコンテナイメージを取得する。

```bash
$ docker pull cloudera/quickstart:latest
$ docker images
REPOSITORY            TAG                 IMAGE ID            CREATED             SIZE
cloudera/quickstart   latest              584a24e9ca7b        3 months ago        6.213 GB
```

### Dockerコンテナの起動とHBase初期設定

```bash
$ docker run --hostname=quickstart.cloudera --privileged=true -t -i -p 80:80 -p 2181:2181 -p 7180:7180 -p 60000:60000 -p 60010:60010 -p 60020:60020 cloudera/quickstart /usr/bin/docker-quickstart
```

(ポートはこんなに開けなくてもいいかもしれない。ZooKeeperクライアント接続:2181とZooKeeperが振り分けるであろうHBaseのregion server node:60020は必要。)  
参考: http://blog.cloudera.com/blog/2013/07/guide-to-using-apache-hbase-ports/

仮想OSのメモリが不十分(8GB)だとhue起動が失敗するが気にしない。

#### HBaseのテーブル作成

こんな感じのテーブルを作る

テーブル名:test1
カラム(ファミリ)名:data1,data2

|ROWID|data1|data2|
|--------|-------|------|
|0         |001    |dog  |
|1         |(なし) |cat    |
|2         |003    |(なし)|

↓ここからDockerコンテナ内(シェル)の操作

* Dockerコンテナを落とさずに抜けるにはCtrl+P, Ctrl+Q(docker ps でコンテナIDを控え、docker attach コンテナID で復帰)
* 落として抜けるにはexit(Dockerイメージには作業内容は保存されないので、もし作業状態を残したイメージを作りたければdocker commitする。)
 
```bash
[root@quickstart /]# hbase shell
hbase(main):003:0> create 'test1', 'data1', 'data2'
0 row(s) in 0.2940 seconds
```

'test1'テーブルの作成

```bash
hbase(main):003:0> create 'test1', 'data1', 'data2'
0 row(s) in 0.2940 seconds
=> Hbase::Table - test1
hbase(main):004:0> describe 'test1'
Table test1 is ENABLED                                                          
test1                                                                           
COLUMN FAMILIES DESCRIPTION                                                     
 {NAME => 'data1', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATI
ON_SCOPE => '0', VERSIONS => '1', COMPRESSION => 'NONE', MIN_VERSIONS => '0', TT
L => 'FOREVER', KEEP_DELETED_CELLS => 'FALSE', BLOCKSIZE => '65536', IN_MEMORY =
> 'false', BLOCKCACHE => 'true'}                                                
{NAME => 'data2', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATI
ON_SCOPE => '0', VERSIONS => '1', COMPRESSION => 'NONE', MIN_VERSIONS => '0', TT
L => 'FOREVER', KEEP_DELETED_CELLS => 'FALSE', BLOCKSIZE => '65536', IN_MEMORY =
> 'false', BLOCKCACHE => 'true'}                                                
2 row(s) in 0.0240 seconds
```

#### データの挿入

行ごとではなく行のカラムごとに入れていくため、コマンド発行数は4回ある

```bash
hbase(main):006:0> put 'test1', '0', 'data1', '001'  
0 row(s) in 0.0750 seconds  
hbase(main):007:0> put 'test1', '0', 'data2', 'dog'  
0 row(s) in 0.0170 seconds  
hbase(main):009:0> put 'test1', '1', 'data2', 'cat'  
0 row(s) in 0.0110 seconds  
hbase(main):010:0> put 'test1', '2', 'data1', '003'  
0 row(s) in 0.0050 seconds  
hbase(main):011:0> scan 'test1'  
ROW                   COLUMN+CELL  
 0                    column=data1:, timestamp=1457903867672, value=001         
 0                    column=data2:, timestamp=1457903885345, value=dog         
 1                    column=data2:, timestamp=1457903915353, value=cat         
 2                    column=data1:, timestamp=1457903926365, value=003         
3 row(s) in 0.0280 seconds
```

Dockerコンテナ内の作業はここまで。(コンテナは落とさないこと）

### IDEからの疎通(java client->HBase)

IDE(Eclipse,IntelliJなどで)org.yggd.hbase.sample.HBaseClientを実行してみる。
javaのAPIは変遷が激しく比較的新しいものを使用しているが 、古いバージョンでは使えないかもしれない。

注意：クライアント環境がWindowsの場合、JNIでHadoopのバイナリ(winutils.exe)が呼ばれることがあり、ないと怒られる。
[ここ](https://osdn.jp/projects/win-hadoop/)からwinutils.exe入手して`C:¥winutils¥bin`に配置し、
VM起動オプションでシステムプロパティ`-Dhadoop.home.dir=C:¥winutils`(binは入れないこと)を指定する。

以下のログが表示されたら疎通は成功している。

```
[INFO ] **** Scan to test1  
[INFO ]     data1[001] data2[dog]  
[INFO ]     data1[null] data2[cat]  
[INFO ]     data1[003] data2[null]
```

### IDEからの疎通(java client(Spark)->HBase)

Scalaでやったほうが簡単だけどあえてJavaでやる。
VM起動オプションに以下を設定して、org.yggd.spark.sample.SparkClientを実行する。

```
-Dorg.xerial.snappy.lib.name=libsnappyjava.jnilib -Dorg.xerial.snappy.tempdir=/tmp
```
 以下のログが表示されていたら疎通は成功している。
 
 ```
 [INFO ] **** Scan to test1
 (省略)
 [INFO ]     KEY[0] VALUE(data1)[001] VALUE(data2)[dog]
 [INFO ]     KEY[1] VALUE(data1)[null] VALUE(data2)[cat]
 [INFO ]     KEY[2] VALUE(data1)[003] VALUE(data2)[null]
 ```
 
