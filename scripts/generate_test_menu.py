#!/usr/bin/env python3
"""
Generate a test menu image with Japanese text for OCR testing.

Requirements:
    pip install Pillow

Usage:
    python scripts/generate_test_menu.py
"""

from PIL import Image, ImageDraw, ImageFont
import os

def create_test_menu():
    # Image dimensions
    width, height = 800, 600

    # Create white background
    image = Image.new('RGB', (width, height), 'white')
    draw = ImageDraw.Draw(image)

    # Try to use a font that supports Japanese characters
    try:
        # Try system fonts that support Japanese
        font_large = ImageFont.truetype('/System/Library/Fonts/ヒラギノ角ゴシック W4.ttc', 60)
        font_medium = ImageFont.truetype('/System/Library/Fonts/ヒラギノ角ゴシック W4.ttc', 40)
    except (OSError, IOError):
        try:
            font_large = ImageFont.truetype('/System/Library/Fonts/Hiragino Sans GB.ttc', 60)
            font_medium = ImageFont.truetype('/System/Library/Fonts/Hiragino Sans GB.ttc', 40)
        except (OSError, IOError):
            # Fallback to default font
            font_large = ImageFont.load_default()
            font_medium = ImageFont.load_default()

    # Menu items in Japanese
    menu_items = [
        ("メニュー", 100, 'black', font_large),  # Title: "Menu"
        ("スシ        ¥1000", 200, 'black', font_medium),  # Sushi ¥1000
        ("ラーメン    ¥800", 280, 'black', font_medium),   # Ramen ¥800
        ("天ぷら      ¥1200", 360, 'black', font_medium),  # Tempura ¥1200
        ("カレー      ¥900", 440, 'black', font_medium),   # Curry ¥900
    ]

    # Draw menu items
    for text, y_position, color, font in menu_items:
        draw.text((100, y_position), text, fill=color, font=font)

    # Save image
    output_path = os.path.join(
        os.path.dirname(__file__),
        '../backend/src/test/resources/images/test-menu.jpg'
    )
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    image.save(output_path, 'JPEG', quality=95)
    print(f"✅ Test menu image created: {output_path}")

    # Print image info
    file_size = os.path.getsize(output_path)
    print(f"📏 Image size: {width}x{height}")
    print(f"💾 File size: {file_size / 1024:.2f} KB")

if __name__ == '__main__':
    create_test_menu()
