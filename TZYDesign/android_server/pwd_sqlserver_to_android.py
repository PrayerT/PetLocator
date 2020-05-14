# coding=utf-8
from pymysql import connect
import socket
import re


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
    tcp_server_socket.bind(("", 6172))
    tcp_server_socket.listen(128)
    while True:
        new_client_socket, client_addr = tcp_server_socket.accept()
        name = new_client_socket.recv(1024).decode("utf8")
        name = re.sub(r"\r\n", "", name)
        print(name)
        sql = "select pwd from usrInfo where name=%s"
        count = cs.execute(sql, [name])
        if count == 1:
            record = cs.fetchone()
            pwd = record[0] + "\r\n"
            try:
                new_client_socket.send(pwd.encode("utf8"))
                print("已接收姓名为：" + name + "的请求，发送密码：" + pwd)
            except:
                print("disconnect!")
        else:
            try:
                reply = "no record!"
                new_client_socket.send(reply.encode("utf8"))
                print(reply)
            except:
                print("disconnect!")

    tcp_server_socket.close()

    cs.close()
    conn.close()


if __name__ == '__main__':
    main()
