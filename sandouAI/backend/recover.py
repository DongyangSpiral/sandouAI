import json
import re

log_file = r'C:\Users\shenx\.gemini\antigravity\brain\c2a398dd-77bd-41d4-bf51-61cf0f5bffd8\.system_generated\logs\overview.txt'
files_to_recover = [
    'AIController.java',
    'TeamController.java',
    'TeamFileController.java',
    'AIService.java',
    'TeamLogService.java',
    'TeamService.java'
]

recovered = {}
for line in open(log_file, 'r', encoding='utf-8'):
    if line.startswith(':'):
        line = line[1:]
    try:
        data = json.loads(line)
        if 'tool_calls' in data:
            for tc in data['tool_calls']:
                if tc.get('name') == 'write_to_file':
                    args = tc.get('args', {})
                    target = args.get('TargetFile', '').strip('"')
                    for f in files_to_recover:
                        if target.endswith(f):
                            recovered[target] = args.get('CodeContent', '').strip('"')
    except Exception as e:
        pass

for target, content in recovered.items():
    if content:
        # CodeContent is JSON stringified, we need to decode escapes like \n
        content = content.encode('utf-8').decode('unicode_escape')
        # TargetFile might have escaped backslashes e.g. e:\\Code\\
        target = target.replace('\\\\', '\\')
        with open(target, 'w', encoding='utf-8') as f:
            f.write(content)
        print("Recovered", target)
