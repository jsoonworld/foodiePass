# Worktree Optimization Guide

## 📊 Current Worktree Status

### Active Worktrees (2025-11-06)

```bash
git worktree list
```

Output:
```
/Users/harperkwon/Desktop/github/projects/foodiePass                            [feature/mvp-frontend]
/Users/harperkwon/Desktop/github/projects/foodiePass-abtest                     [feature/mvp-abtest]
/Users/harperkwon/Desktop/github/projects/foodiePass-backend                    [feature/mvp-backend-integration]
/Users/harperkwon/Desktop/github/projects/foodiePass-e2e                        [feature/mvp-e2e-deployment]
/Users/harperkwon/Desktop/github/projects/foodiePass-menu-api                   [feature/mvp-menu-api]
/Users/harperkwon/Desktop/github/projects/foodiePass-survey                     [feature/mvp-survey]
/Users/harperkwon/Desktop/github/projects/foodiePass/foodiePass-backend-gemini  [feature/backend-gemini-fix]
/Users/harperkwon/Desktop/github/projects/foodiePass/foodiePass-develop         [develop]
/Users/harperkwon/Desktop/github/projects/foodiePass/foodiePass-docs            [feature/docs-session-handoff]
```

**Total**: 9 worktrees (1 main + 8 additional)

---

## 🚨 Current Issues

### 1. Branch Status Confusion
**Problem**: Too many worktrees make it difficult to track which branches are active

**Impact**:
- Risk of working on outdated branches
- Confusion about merge status
- Difficult to identify current work location

### 2. Nested Worktrees
**Problem**: Some worktrees are inside the main repository directory
- `foodiePass/foodiePass-backend-gemini`
- `foodiePass/foodiePass-develop`
- `foodiePass/foodiePass-docs`

**Impact**:
- Git operations may be slower
- Risk of accidental commits to wrong repo
- Confusing directory structure

### 3. Merged Branches Still Active
**Problem**: Some feature branches may already be merged to develop

**Impact**:
- Wasting disk space
- Potential confusion about active work
- Risk of duplicate work

---

## ✅ Recommended Cleanup Strategy

### Phase 1: Identify Merged Branches

```bash
cd /Users/harperkwon/Desktop/github/projects/foodiePass

# Check which branches are merged to develop
git checkout develop
git pull origin develop

# List merged branches
git branch --merged develop

# Check remote merge status
git branch -r --merged develop
```

### Phase 2: Remove Completed Worktrees

For each merged feature branch:

```bash
# Example: feature/mvp-abtest (if merged)
git worktree remove /Users/harperkwon/Desktop/github/projects/foodiePass-abtest

# Optional: Delete local branch
git branch -d feature/mvp-abtest

# Optional: Delete remote branch (after confirming merge)
git push origin --delete feature/mvp-abtest
```

**Candidates for removal** (verify merge status first):
1. `foodiePass-abtest` (feature/mvp-abtest) - if merged to develop
2. `foodiePass-survey` (feature/mvp-survey) - if merged to develop
3. `foodiePass-menu-api` (feature/mvp-menu-api) - if merged to develop
4. `foodiePass-backend-gemini` (feature/backend-gemini-fix) - if merged

### Phase 3: Consolidate Active Work

**Current active branch**: `feature/mvp-frontend`

**Strategy**:
1. Keep only the main worktree for active development
2. Use `git checkout` for occasional branch switches
3. Only create worktrees for truly parallel development

**Remove nested worktrees**:
```bash
# From main repo
cd /Users/harperkwon/Desktop/github/projects/foodiePass

# Remove nested worktrees
git worktree remove foodiePass-backend-gemini
git worktree remove foodiePass-develop
git worktree remove foodiePass-docs
```

### Phase 4: Simplified Worktree Structure

**Recommended final state**:
```
projects/
├── foodiePass/                # main repo - develop branch
├── foodiePass-frontend/       # feature/mvp-frontend (if still needed)
└── foodiePass-e2e/           # feature/mvp-e2e-deployment (for testing)
```

**Total**: 1-3 worktrees maximum

---

## 🎯 Best Practices Going Forward

### When to Use Worktrees

✅ **Good use cases:**
- Long-running feature branches that need parallel development
- Testing/QA on separate branch while continuing development
- Reviewing PRs without disrupting current work
- Emergency hotfixes while feature work is in progress

❌ **Avoid for:**
- Short-lived feature branches (< 1 day)
- Simple bug fixes
- Documentation updates
- Branches that depend on each other

### Naming Convention

```bash
# External worktrees (outside main repo)
foodiePass-[feature-name]/

# Examples:
foodiePass-frontend/
foodiePass-backend/
foodiePass-mobile/
```

**Never nest worktrees inside main repo**

### Maintenance Schedule

**Weekly check**:
```bash
# Review active worktrees
git worktree list

# Check for stale branches
git branch --merged develop

# Clean up prunable worktrees
git worktree prune
```

**After feature completion**:
1. Merge to develop
2. Remove worktree immediately
3. Delete feature branch (local and remote)

---

## 🔧 Quick Commands Reference

### Check Worktree Status
```bash
# List all worktrees
git worktree list

# List with details
git worktree list --porcelain
```

### Remove Worktree
```bash
# Remove and delete working directory
git worktree remove <path>

# Remove without deleting (if manually deleted)
git worktree prune
```

### Add New Worktree (if needed)
```bash
# Create new worktree from existing branch
git worktree add ../foodiePass-feature feature/branch-name

# Create new worktree with new branch
git worktree add -b feature/new-feature ../foodiePass-new-feature develop
```

### Move to Different Branch in Worktree
```bash
cd /path/to/worktree
git checkout other-branch
```

### Check Branch Merge Status
```bash
# Check if branch is merged to develop
git branch --merged develop | grep feature/branch-name

# Check commits not in develop
git log develop..feature/branch-name --oneline
```

---

## 📋 Cleanup Checklist

### Before Removing a Worktree

- [ ] Verify branch is fully merged to develop
- [ ] Check for uncommitted changes: `git status`
- [ ] Check for unpushed commits: `git log origin/branch..branch`
- [ ] Backup any important local changes
- [ ] Verify tests pass on develop branch

### Cleanup Commands

```bash
# 1. Check what will be removed
git worktree list

# 2. Remove external worktrees (examples)
git worktree remove /Users/harperkwon/Desktop/github/projects/foodiePass-abtest
git worktree remove /Users/harperkwon/Desktop/github/projects/foodiePass-survey
git worktree remove /Users/harperkwon/Desktop/github/projects/foodiePass-menu-api
git worktree remove /Users/harperkwon/Desktop/github/projects/foodiePass-backend

# 3. Remove nested worktrees (from main repo)
cd /Users/harperkwon/Desktop/github/projects/foodiePass
git worktree remove foodiePass-backend-gemini
git worktree remove foodiePass-develop
git worktree remove foodiePass-docs

# 4. Verify cleanup
git worktree list

# 5. Optional: Delete merged branches
git branch --merged develop | grep feature/ | xargs git branch -d
```

---

## 🎓 Learning Resources

### Git Worktree Documentation
- [Official Git Worktree Docs](https://git-scm.com/docs/git-worktree)
- [Pro Git - Git Tools](https://git-scm.com/book/en/v2/Git-Tools-Advanced-Merging)

### When to Use Worktrees
- **Use Case 1**: Emergency hotfix while feature is in progress
- **Use Case 2**: Reviewing large PRs without losing local work
- **Use Case 3**: Long-running parallel feature development (rare)

### Alternatives to Worktrees
- **Git stash**: For temporary context switching
- **Feature branches**: Standard workflow for most cases
- **Git checkout**: Fast branch switching for simple cases

---

## 📞 Decision Matrix

| Situation | Recommended Approach |
|-----------|---------------------|
| Quick bug fix | `git stash` + `git checkout` |
| Review PR | `git stash` + `git checkout` or GitHub web UI |
| Short feature (<1 day) | Regular feature branch |
| Long feature (multiple days) | Consider worktree |
| Parallel features | Worktrees (2-3 max) |
| Emergency hotfix during feature | Worktree |

---

## 🎯 Target State

After cleanup, maintain:
- **1 main worktree** (develop or current feature)
- **0-2 additional worktrees** (only for active parallel work)
- **Clean branch list** (delete merged branches)

**Benefits**:
- Clear mental model of current work
- Faster git operations
- Less risk of working on wrong branch
- Easier to onboard new contributors

---

Last Updated: 2025-11-06
Next Review: After feature/mvp-frontend merge to develop
