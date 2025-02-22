# Git Rebase vs Merge

## Merge
Creates a merge commit. Preserves full history.
```
main:    A---B---C---M
              \     /
feature:       D---E
```
```bash
git checkout main
git merge feature
```

## Rebase
Replays commits on top of target branch. Linear history.
```
main:    A---B---C
                  \
feature:           D'---E'
```
```bash
git checkout feature
git rebase main
git checkout main
git merge feature  # fast-forward
```

## Interactive Rebase
Clean up commits before merging:
```bash
git rebase -i HEAD~3
# pick, squash, fixup, reword, drop
```

## When to Use What
| Scenario | Strategy |
|----------|----------|
| Feature branch → main | Squash merge (one clean commit) |
| Long-lived branch sync | Rebase (keep branch up to date) |
| Public/shared branches | Merge (don't rewrite history) |
| Cleaning commit history | Interactive rebase |

## Golden Rule
**Never rebase commits that have been pushed and shared with others.**

## My Workflow
```bash
# On feature branch, stay up to date
git fetch origin
git rebase origin/main

# When ready to merge
git checkout main
git merge --squash feature
git commit -m "feat: add user authentication"
```
