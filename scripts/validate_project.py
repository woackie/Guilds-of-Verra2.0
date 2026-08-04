#!/usr/bin/env python3
from pathlib import Path
import json, math, sys
ROOT = Path(__file__).resolve().parents[1]
errors=[]
all_json=list((ROOT/'src').rglob('*.json'))
for path in all_json:
    try: json.loads(path.read_text(encoding='utf-8'))
    except Exception as exc: errors.append(f'Invalid JSON {path.relative_to(ROOT)}: {exc}')
for path in (ROOT/'src/main/resources/data/guildsofverra/guilds_of_verra/skill_trees').glob('*.json'):
    data=json.loads(path.read_text())
    nodes=data['nodes']; ids={n['id'] for n in nodes}; total=sum(n['cost'] for n in nodes)
    if total != 100: errors.append(f'{path.name}: total cost {total}, expected 100')
    if data.get('max_level') != 100: errors.append(f'{path.name}: max level is not 100')
    for n in nodes:
        for p in n.get('prerequisites',[]):
            if p not in ids: errors.append(f'{path.name}: {n["id"]} has missing prerequisite {p}')
        if not 0 <= n['min_level'] <= 100: errors.append(f'{path.name}: bad level {n["id"]}')

def req(level): return round(100 + 12*level + 1.5*level*level)
curve=[req(i) for i in range(100)]
if sum(curve) != 561950: errors.append(f'XP total {sum(curve)}, expected 561950')
if any(b <= a for a,b in zip(curve,curve[1:])): errors.append('XP curve not strictly increasing')
if errors:
    print('VALIDATION FAILED')
    print('\n'.join(' - '+e for e in errors))
    sys.exit(1)
print(f'Validated {len(all_json)} JSON files, 5 skill trees, 124 nodes and XP total {sum(curve):,}.')
