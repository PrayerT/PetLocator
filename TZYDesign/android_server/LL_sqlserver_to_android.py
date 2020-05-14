# coding=utf-8
from pymysql import connect
import socket


def main():
    conn = connect(
        host="localhost",
        port=3306,
        user="root",
        password="prayer",
        database="nav_test",
        charset="utf8"
    )
    cs = conn.cursor()

    tcp_server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    tcp_server_socket.bind(("0.0.0.0", 6170))
    tcp_server_socket.listen(128)
    while True:
        c = 0
        new_client_socket, client_addr = tcp_server_socket.accept()
        sql = "select longitude,latitude from dist where id=1"
        count = cs.execute(sql)
        record = cs.fetchone()
        longitude = record[0]
        latitude = record[1]
        sendlon = str(record[0]) + "\r\n"
        sendlat = str(record[1]) + "\r\n"
        # sendlatlon = str(record[0]) + '\r\n' + str(record[1])
        if c == 0:
            try:
                new_client_socket.send(sendlon.encode("utf8"))
                new_client_socket.send(sendlat.encode("utf8"))
                print("发送...")
                print("经度:" + longitude + ", 纬度:" + latitude)
                c = c + 1
            except:
                print("disconnect!")
        else:
            break

    tcp_server_socket.close()

    cs.close()
    conn.close()


if __name__ == '__main__':
    main()
