#!/usr/bin/env python3

import os

import argparse
import csv

EMPTY_LINE = ''

GENERATED_PLACEHOLDER_SENTINEL_TEXT = 'GENERATED_PLACEHOLDER_SENTINEL_TEXT'

CARD_INTERFACE_MAP = {
    'CHARACTER_ALIEN': 'AbstractAlien',
    'CHARACTER_REBEL': 'AbstractRebel',
    'LOCATION_SITE': 'AbstractSite',
    'LOCATION_SYSTEM': 'AbstractSystem',
}

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('-c', '--csv_file', type=str, required=True)
    parser.add_argument('-i', '--card_index', type=str, required=False)
    parser.add_argument('-f', '--force', action=argparse.BooleanOptionalAction)
    parser.add_argument('-t', '--title_explicit', action=argparse.BooleanOptionalAction)
    args = parser.parse_args()
    
    if args.force and not args.card_index:
        print('ERROR: -f/--force requires -i/--card_index')
        exit(1)

    with open(args.csv_file, 'r' ) as f:
        reader = csv.DictReader(f)
        for record in reader:
            if args.card_index is None or record['card_index'] == args.card_index:
                try_generate_classfile_from_record(record, args.force, args.title_explicit)

def try_generate_classfile_from_record(record: dict, force: bool, use_explicit_title: bool):
    # acquire metadata
    card_side = record['card_side']
    card_class_name = generate_card_class_name(record)
    
    # calculate path for generated file relative to this file to avoid surprises...
    script_directory = os.path.dirname(os.path.realpath(__file__))
    card_directory = os.path.join(script_directory, card_side.lower())

    card_filepath = os.path.join(card_directory, f'{card_class_name}.java')

    already_exists = os.path.isfile(card_filepath)
    if already_exists and force:
        print(f'[{card_class_name}] replacing: force specified')
    elif already_exists and not force:
        # check for sentinel text to see if the file has been manually modified
        with open(card_filepath, 'r') as f:
            if not GENERATED_PLACEHOLDER_SENTINEL_TEXT in f.read():
                # bail to avoid clobbering work...
                print(f'[{card_class_name}] skipping: autogen sentinel missing (override with -f)')
                return
            else:
                print(f'[{card_class_name}] replacing: autogen sentinel still present')
    else: # does not already exist
        print(f'[{card_class_name}] creating: new file')

    # ...(re)generate the file, in advance of file creation in case it fails...
    codegen = generate_class(record, use_explicit_title)

    # ...and write it out, ensuring directory exists:
    os.makedirs(card_directory, exist_ok=True)
    with open(card_filepath, 'w') as f:
        f.write(codegen)

# Hardcoded 4-space indentation
def indent(indentations: int, text: str | None) -> str:
    # passthrough:
    if text is None:
        return None

    prefix = ' ' * (4 * indentations)
    return f'{prefix}{text}'

def indent_list(indentations: int, items: list[str]) -> list[str]:
    return [indent(indentations, item) for item in items]

def generate_card_class_name(record) -> str:
    set_id = record['set_id']
    card_index = record['card_index']

    # Set has no leading zeros but card index is always three digits.
    # Card index could contain "_BACK", so get clever with it:
    index_parts = card_index.split('_')
    card_index_ordinal = int(index_parts[0])  # can't 3-digit format a string

    output_parts = [
        f'Card{set_id}',
        f'{card_index_ordinal:03d}',
    ]

    # last element optional, presumed to be either "BACK" or missing here, either of which is desirable:
    if len(index_parts) > 1:
        output_parts.append(index_parts[1])

    return '_'.join(output_parts)

def generate_class(record, use_explicit_title) -> str:
    card_type = record['card_type']

    card_class_name = generate_card_class_name(record)
    card_interface_name = CARD_INTERFACE_MAP[card_type]

    generated_lines = [
        # package
        generate_package(record),
        # imports
#        *generate_multi_java_imports_section(record),  # used by location template
        EMPTY_LINE,
        f'import com.gempukku.swccgo.cards.{card_interface_name};',
        *generate_multi_common_imports(record),
#        *generate_multi_location_imports(record),  # used by location template
        # placeholder
        EMPTY_LINE,
        indent(0, f'// TODO: this {GENERATED_PLACEHOLDER_SENTINEL_TEXT} allows autogeneration to clobber this file.  Delete this line to protect the file.'),
        # doc comment
        EMPTY_LINE,
        *generate_multi_doc_comment(record),
        # class header
        indent(0, f'public class {card_class_name} extends {card_interface_name} {{'),
        indent(1, f'public {card_class_name}() {{'),
        # instantiation
        indent(2, generate_super(record, use_explicit_title)),
        # attributes - card text
        indent(2, opt_generate_text_declaration(record, 'setLore', 'lore_text')),
        indent(2, opt_generate_text_declaration(record, 'setGameText', 'game_text')),
        indent(2, opt_generate_text_declaration(record, 'setLocationLightSideGameText', 'light_side_text')),
        indent(2, opt_generate_text_declaration(record, 'setLocationDarkSideGameText', 'dark_side_text')),
        # attributes - other (alphabetical order)
        *indent_list(2, multi_generate_force_gen_icons_declation(record)),
        indent(2, opt_generate_enum_declaration(record, 'addIcons', 'Icon', 'icons')),
        indent(2, opt_generate_enum_declaration(record, 'addKeywords', 'Keyword', 'keywords')),
        indent(2, opt_generate_enum_declaration(record, 'addPersonas', 'Persona', 'persona')),
        indent(2, opt_generate_enum_declaration(record, 'setSpecies', 'Species', 'species')),
        indent(1, '}'),
        # TODOs
        *indent_list(1, multi_generate_todos_section_from_game_text(record)),
        EMPTY_LINE,
        # class footer
        indent(0, '}'),
        # trailing newline
        EMPTY_LINE,
    ]

    return '\n'.join(filter(lambda x: x is not None, generated_lines))

def generate_package(record) -> str:
    card_side = record['card_side']
    set_id = record['set_id']
    return f'package com.gempukku.swccgo.cards.set{set_id}.{card_side.lower()};'

def generate_multi_java_imports_section(record) -> list[str]:
    # location template uses these
    if 'light_side_text' in record or 'dark_side_text' in record:
        return [
            EMPTY_LINE,
            'import java.util.LinkedList;',
            'import java.util.List;',
        ]
    else:
        return []

def generate_multi_common_imports(record) -> list[str]:
    # auto-detect which classes the autogenerated code references
    # pull None if missing to filter out later
    classes_to_import = [
       # hardcoded in super() declaration
       'Title',
       'Uniqueness',
       'ExpansionSet',
       'Rarity',
       'Side',
    ]

    # dependent on attributes
    if 'icons' in record:
        classes_to_import.append('Icon')
    if 'keywords' in record:
        classes_to_import.append('Keyword')
    if 'species' in record:
        classes_to_import.append('Species')
    if 'persona' in record:
        classes_to_import.append('Persona')

    # sort alphabetically like a good citizen and format
    return [f'import com.gempukku.swccgo.common.{c};' for c in sorted(classes_to_import)]

def generate_multi_location_imports(record) -> list[str]:
    # location template uses these
    if 'light_side_text' in record or 'dark_side_text' in record:
        return [
            'import com.gempukku.swccgo.game.PhysicalCard;',
            'import com.gempukku.swccgo.game.SwccgGame;',
            'import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;',
        ]
    else:
        return []


# FIXME: this doesn't exactly match the info in existing comments
# but is more useful for searching IMO
# as it matches internal names
def generate_multi_doc_comment(record) -> list[str]:
    return [
        '/**',
        f'* Set: {record["expansion_set"]}',
        f'* Type: {record["card_type"]}',
        #'* Subtype: FIXME: implement this?',
        f'* Title: {record["card_title"]}',
        '*/',
    ]

def to_title_enum(card_title: str) -> str:
    # reasonable guess; all uppercase, all spaces to underscores, strip non-alphanumerics
    return ''.join(ch if ch.isalpha() else '_' for ch in card_title.replace(' ', '_')).upper()

def generate_super(record, use_explicit_title: bool) -> str:
    # all rarities are "V" (Virtual)
    # super() implementation is dependent on card type:
    card_type = record['card_type']
    card_title = f'"{record["card_title"]}"' if use_explicit_title else f'Title.{to_title_enum(record["card_title"])}'

    if card_type == 'CHARACTER_ALIEN':
        return f'super(Side.{record["card_side"]}, {record["destiny"]}, {record["deploy"]}, {record["power"]}, {record["ability"]}, {record["forfeit"]}, {card_title}, Uniqueness.{record["uniqueness"]}, ExpansionSet.{record["expansion_set"]}, Rarity.V);'
    elif card_type == 'LOCATION_SITE':
        return f'super(Side.{record["card_side"]}, {card_title}, Title.{record["system_name"]}, Uniqueness.{record["uniqueness"]}, ExpansionSet.{record["expansion_set"]}, Rarity.V);'
    else:
        raise RuntimeError(f'Missing generate_super() implementation for card type: {card_type}')

def opt_generate_text_declaration(record, func_name, record_index) -> str:
    text = record[record_index]
    if not text:
        return None
    else:
        # text is quoted; strip whitespace in case of inconsistent input
        return f'{func_name}("{text.strip()}");'

def multi_generate_force_gen_icons_declation(record) -> list[str]:
    # N.B. assumption here is these are integers
    force_gen_dark = record.get('force_gen_dark')
    force_gen_light = record.get('force_gen_light')

    res = []
    if force_gen_dark:
        num_dark_force = int(force_gen_dark)
        if num_dark_force > 0:
            res.append(f'addIcon(Icon.DARK_FORCE, {num_dark_force});')
    if force_gen_light:
        num_light_force = int(force_gen_light)
        if num_light_force > 0:
            res.append(f'addIcon(Icon.LIGHT_FORCE, {num_light_force});')
    return res

def opt_generate_enum_declaration(record, func_name, enum_name, record_index):
    values = record[record_index]
    if not values:
        return None
    else:
        # assumption here that input schema is a comma-separated list of enum values,
        # and that values contain no spaces
        values_cleaned = values.replace(' ', '').split(',')
        func_args = ', '.join([f'{enum_name}.{value}' for value in sorted(values_cleaned)])
        # arguments are not quoted
        return f'{func_name}({func_args});'

def multi_generate_todos_section_from_game_text(record) -> list[str]:
    game_text = record.get('game_text')
    light_side_text = record.get('light_side_text')
    dark_side_text = record.get('dark_side_text')

    res = [EMPTY_LINE]
    if game_text:
        # remove trailing period and put on multiple linesfor swag and no other reason
        res.extend([f'// TODO: {sentence}' for sentence in game_text.split('. ')])
    if dark_side_text:
        res.append(f'// DARK SIDE TODO: {dark_side_text}')
    if light_side_text:
        res.append(f'// LIGHT SIDE TODO: {light_side_text}')
    return res

if __name__ == "__main__":
    main()
