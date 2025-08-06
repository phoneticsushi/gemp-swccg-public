#!/usr/bin/env python3

import os

import argparse
import csv


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('-c', '--csv_file', type=str, required=True)
    args = parser.parse_args()
    
    with open(args.csv_file, 'r' ) as f:
        reader = csv.DictReader(f)
        for record in reader:
            print(generate_image_url_mapping(record))

def generate_image_url_mapping(record: dict):
    set_id = record['set_id']
    card_index = record['card_index']
    card_side = record['card_side']
    image_name = record['image_name']

    # N.B. card index can't have leading zeros here, but it doesn't in the spreadsheet so it's fine
    key = f'{set_id}_{card_index}'

    # lol, cloudflare
    value = f'https://pub-56b484789f1c4698a152cb0e72ca68c8.r2.dev/beezer_bowl_2025/images/{card_side.lower()}/{image_name}'

    return f'"{key}": "{value}",'

if __name__ == "__main__":
    main()
