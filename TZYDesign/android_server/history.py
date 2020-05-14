from pymysql import connect
import socket
import re
from time import sleep


def main():
    conn = connect(
        host="172.20.10.8",
        port=3306,
        user="root",
        password="password",
        database="pyTest",
        charset="utf8"
    )
    cs = conn.cursor()

    tcp_server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    tcp_server_socket.bind(("", 10000))
    while True:
        return_list = ""
        tcp_server_socket.listen(128)
        new_client_socket, client_addr = tcp_server_socket.accept()
        req = new_client_socket.recv(1024).decode("utf8")
        print(req);
        req = re.sub(r"\r\n", "", req)
        req = req.split(",")
        print(req)
        Name, Date = req[0], req[1]
        if Name == "" and Date == "":
            sql = """select name, time from entrance"""
            count = cs.execute(sql)
        elif Name == "" and Date != "":
            sql = """select name, time from entrance where date(time)=%s"""
            count = cs.execute(sql, [Date])
        elif Name != "" and Date == "":
            sql = """select name, time from entrance where name=%s"""
            count = cs.execute(sql, [Name])
        elif Name != "" and Date != "":
            sql = """select name, time from entrance where name=%s and date(time)=%s"""
            count = cs.execute(sql, [Name, Date])
        else:
            print("[ERROR] A unknown error occurred!")

        if count != 0:
            result = cs.fetchall()
            return_list += str(count) + "\r\n";
            for i in result:
                print(i)
                return_list += i[0] + "," + str(i[1]) + "\r\n"
            try:
                new_client_socket.send(return_list.encode("utf8"))
                print("send successfully")
            except ConnectionAbortedError:
                print("disconnect!")
        else:
            try:
                reply = "0\r\n no record!" + " " + "\r\n"
                new_client_socket.send(reply.encode("utf8"))
                print(reply)
            except ConnectionAbortedError:
                print("disconnect!")

    tcp_server_socket.close()

    cs.close()
    conn.close()


if __name__ == '__main__':
    main()
