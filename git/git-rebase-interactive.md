# Git Rebase — Interactive Workflows

## Rebase vs Merge

```
Merge: Creates a merge commit, preserves full history
  A─B─C─────M  (main)
       \   /
        D─E    (feature)

Rebase: Replays commits on top, linear history
  A─B─C─D'─E'  (feature rebased onto main)
```

## Interactive Rebase

```bash
# Rewrite last 4 commits
git rebase -i HEAD~4
```

Opens editor with:
```
pick abc1234 Add user model
pick def5678 Fix typo in user model
pick ghi9012 Add user API endpoint
pick jkl3456 Add user tests
```

## Commands

| Command | Effect |
|---------|--------|
| `pick` | Keep commit as-is |
| `reword` | Keep commit, edit message |
| `edit` | Pause to amend commit |
| `squash` | Meld into previous commit, combine messages |
| `fixup` | Meld into previous, discard this message |
| `drop` | Remove commit entirely |
| `reorder` | Move lines to reorder commits |

## Common Workflows

### Squash messy commits before PR

```bash
git rebase -i main
# Mark all but first as 'squash' or 'fixup'
```

### Split a commit

```bash
git rebase -i HEAD~3
# Mark commit as 'edit'
git reset HEAD^              # unstage changes
git add -p                   # stage selectively
git commit -m "Part 1"
git add .
git commit -m "Part 2"
git rebase --continue
```

### Autosquash with fixup commits

```bash
# While working, create fixup commits
git commit --fixup=abc1234

# Later, auto-arrange them
git rebase -i --autosquash main
```

## Rebase onto

```bash
# Move feature branch from old-base to new-base
git rebase --onto new-base old-base feature

# Example: move feature off release back to main
git rebase --onto main release feature
```

## Handling Conflicts

```bash
# During rebase conflict:
git status                    # see conflicting files
# Fix conflicts in editor
git add <resolved-files>
git rebase --continue         # proceed to next commit

# Give up
git rebase --abort            # back to original state
```

## Safety Rules

- **Never rebase published/shared commits** — rewrites history others depend on
- **Use `--force-with-lease`** instead of `--force` when pushing rebased branches
- **Backup before complex rebases**: `git branch backup-feature feature`

## Config Tips

```bash
# Auto-stash dirty working tree before rebase
git config --global rebase.autoStash true

# Always autosquash in interactive rebase
git config --global rebase.autoSquash true
```
