from pathlib import Path
import re

TREE_FILE = "structure.txt"


def clean_name(line: str) -> str:
    """
    Loại bỏ ký tự vẽ cây.
    """
    line = re.sub(r"^[│ ]*", "", line)
    line = line.replace("├── ", "")
    line = line.replace("└── ", "")
    return line.strip()


def get_depth(line: str) -> int:
    """
    Mỗi '│   ' hoặc '    ' là 1 level.
    """
    depth = 0

    i = 0
    while i < len(line):

        if line.startswith("│   ", i):
            depth += 1
            i += 4

        elif line.startswith("    ", i):
            depth += 1
            i += 4

        else:
            break

    return depth


def main():

    root_stack = []

    with open(TREE_FILE, encoding="utf8") as f:

        lines = [x.rstrip("\n") for x in f if x.strip()]

    for raw in lines:

        depth = get_depth(raw)

        name = clean_name(raw)

        is_dir = name.endswith("/")

        if is_dir:
            name = name[:-1]

        while len(root_stack) > depth:
            root_stack.pop()

        if depth == 0:
            current = Path(name)
        else:
            current = root_stack[-1] / name

        if is_dir:

            current.mkdir(parents=True, exist_ok=True)

            root_stack.append(current)

            print(f"[DIR ] {current}")

        else:

            current.parent.mkdir(parents=True, exist_ok=True)

            if not current.exists():
                current.touch()

            print(f"[FILE] {current}")


if __name__ == "__main__":
    main()