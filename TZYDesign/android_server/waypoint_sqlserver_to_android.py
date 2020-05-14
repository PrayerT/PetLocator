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
    tcp_server_socket.bind(("0.0.0.0", 6171))
    tcp_server_socket.listen(128)
    while True:
        return_list = ""
        new_client_socket, client_addr = tcp_server_socket.accept()
        sql = "select longitude,latitude from waypoint"
        count = cs.execute(sql)
        if count != 0:
            result = cs.fetchall()
            return_list += str(count) + "\r\n";
            for i in result:
                print(i)
                return_list += i[0] + "," + str(i[1]) + "\r\n"
            try:
                new_client_socket.send(return_list.encode("utf8"))
                print("send successfully")
            except :
                print("disconnect!")
        else:
            try:
                reply = "0\r\n no record!" + " " + "\r\n"
                new_client_socket.send(reply.encode("utf8"))
                print(reply)
            except :
                print("disconnect!")

    tcp_server_socket.close()

    cs.close()
    conn.close()


if __name__ == '__main__':
    main()
