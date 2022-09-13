import requests
import time
import random

sensors = [
    {"id": "1", "mean": 25},
    {"id": "2", "mean": 0},
    {"id": "3", "mean": 10},
]

if __name__ == "__main__":
    print("Sending temperature values...")

    while True:
        for sensor in sensors:
            temp = int(random.gauss(sensor['mean'], 2))
            print(f"Sensor: {sensor['id']} Temperature: {temp} C")

            response = requests.post("http://localhost:8080/temp", json={"id": sensor["id"], "value": temp})
            print(response)

        time.sleep(1)
