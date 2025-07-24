#!/usr/bin/env python3
import re
import subprocess
import zmq
import time
import os
import random

class ToplletCarlaBridgeMock:
    def __init__(self, time_delta):
        # Check files (as in original)
        self.data_time_delta = time_delta  # s
        self.prev_ego_location = (0.0, 0.0, 0.0)
        # Connect to topllet
        self._connect_to_socket()
        time.sleep(0.1)
        self._send_initial_data()
        ack = self.socket.recv_string()
        if ack != "ACK":
            raise ConnectionError("Initial data could not be sent to Topllet, which replied: " + ack)
        else:
            print("Received ACK")

    def _connect_to_socket(self):
        self.context = zmq.Context()
        self.socket = self.context.socket(zmq.REQ)
        self.socket.connect("tcp://localhost:5555")

    def _send_initial_data(self):
        init_data = """PREFIX : <uc:2#>
ADD :Ego(veh1)
"""
        road_ids = list(range(3))
        lane_ids = list(range(-10,-6))
        for road_id in road_ids:
            init_data += f"ADD :Road(r{road_id})\n"
        for lane_id in lane_ids:
            init_data += f"ADD :Lane(l{lane_id})\n"
            if lane_id < -7:
                init_data += f"ADD :hasLane(r0,l{lane_id})\n"
            else:
                init_data += f"ADD :hasLane(r{random.choice(road_ids)},l{lane_id})\n"
        self.socket.send_string(init_data)
        print("Sent initial data:\n" + init_data)

    def _send_data(self, to_add, to_update, to_delete, is_last=False):
        payload = "PREFIX : <uc:2#>\n"
        for add in to_add:
            if len(add) == 2:
                payload += "ADD: " + add[0] + "(" + add[1] + ")\n"
            else:
                payload += "ADD: " + add[0] + "(" + add[1] + "," + add[2] + ")\n"
        for update in to_update:
            payload += "UPDATE: " + update[0] + "(" + update[1] + "," + update[2] + ")\n"
        for delete in to_delete:
            if len(delete) == 2:
                payload += "DELETE: " + delete[0] + "(" + delete[1] + ")\n"
            else:
                payload += "DELETE: " + delete[0] + "(" + delete[1] + "," + delete[2] + ")\n"
        if is_last:
            payload += "LAST"
        self.socket.send_string(payload)
        print(f"Sent update:\n{payload.strip()}")

    def _fetch_carla_update(self, is_first=False):
        # Fake data generation
        to_add = []
        to_update = []
        to_delete = []
        road_ids = list(range(3))
        lane_ids = list(range(-10,-6))
        if not is_first:
            to_update.append((":isOn", "veh1", f"r{random.choice(road_ids)}"))
            to_update.append((":isOn", "veh1", f"l{random.choice(lane_ids)}"))
        else:
            to_add.append((":isOn", "veh1", f"r{random.choice(road_ids)}"))
            to_add.append((":isOn", "veh1", f"l{random.choice(lane_ids)}"))
        return to_add, to_update, to_delete

    def _parse_query_result(self, query_result):
        result = []
        if query_result == "ACK":
            print("Received ACK")
            return result
        message = query_result.strip()
        if message.startswith('[') and message.endswith(']'):
            message = message[1:-1].strip()
        blocks = re.findall(r'\{[^}]+\}', message)
        for block in blocks:
            d = {}
            block = block.strip()[1:-1]
            pairs = [pair.strip() for pair in block.split(',')]
            for pair in pairs:
                if '=' in pair:
                    key, value = pair.split('=', 1)
                    d[key.strip()] = value.strip()
            result.append(d)
        return result

    def get_update(self, is_first=False, is_last=False):
        result = dict()
        try:
            self._send_data(*self._fetch_carla_update(is_first), is_last=is_last)
            message = self.socket.recv_string()
            result = self._parse_query_result(message)
            print("Result is: " + str(message))
        except zmq.ZMQError as e:
            print(f"ZMQ Error: {e}")
        return result

if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser(description="Mock Topllet-CARLA Bridge")
    parser.add_argument("--time_delta", type=float, default=0.5, help="Time delta between updates")
    args = parser.parse_args()

    bridge = ToplletCarlaBridgeMock(args.time_delta)

    try:
        last = 5
        for i in range(0, last):
            bridge.get_update(is_first=i==0, is_last=i==last-1)
            time.sleep(args.time_delta)
    except KeyboardInterrupt:
        print("\nStopped mock bridge.")
