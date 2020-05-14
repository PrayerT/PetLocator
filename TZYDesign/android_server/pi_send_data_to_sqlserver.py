# coding=utf-8
import socket
from pymysql import connect
import serial
import pynmea2
import time
import datetime
import re


def main():
    time.sleep(1)
    conn = connect(
        host="121.41.104.6",
        port=3306,
        user="root",
        password="prayer",
        database="nav_test",
        charset="utf8"
    )
    cs = conn.cursor()
    while True:
        time.sleep(1)
        ser = serial.Serial("/dev/ttyAMA0", 38400)
        line = ser.readline()
        if line.startswith('$BDRMC'):
            rmc = pynmea2.parse(line)
            a = float(int(float(rmc.lat)/100) + ((float(rmc.lat) - int(float(rmc.lat)/100)*100)/60))
            b = float(int(float(rmc.lon) / 100) + ((float(rmc.lat) - int(float(rmc.lon) / 100) * 100) / 60))
            c = (a, b)
            # print("Latitude:  ", float(rmc.lat) / 100)
            # print("Longitude: ", float(rmc.lon) / 100)
            # a = (float(rmc.lat), float(rmc.lon))
            sql1 = "update dist set longtitude=%f,latitude=%f where id=1"

            now_time = datetime.datetime.now().strftime('%H %M %S')
            hour, minute, second = now_time.split(' ', 2)
            if hour >= 8 & hour <= 20 :
                if minute == 0 :
                    if second == 0 :
                        sql2 = "insert into waypoint set longtitude=%f,latitude=%f"
                        cs.execute(sql2, c)
            cs.execute(sql1, c)
            conn.commit()
            break

    cs.close()
    conn.close()


if __name__ == '__main__':
    main()
