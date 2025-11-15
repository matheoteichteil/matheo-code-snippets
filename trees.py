def max_path_sum(t):
    if t.is_leaf():
        return t.label
    else:
        return t.label + max(max_path_sum(b) for b in t.branches)


def find_path(t, x):
    if t.label == x:
        return [t.label]
    for b in t.branches:
        path = find_path(b, x)
        if path:
            return [t.label] + path


def prune_small(t, n):
    while len(t.branches) > n:
        largest = max(t.branches, key= lambda b: b.label)
        t.branches.remove(largest)
    for b in t.branches:
        prune_small(b, n)

def label_squarer(t):
    t.label = t.label**2
    for b in t.branches:
        label_squarer(b)


def preorder(t):
    result = [t.label]
    for b in t.branches:
        result.extend(preorder(b))
    return result
   
class Tree:
    def __init__(self, label, branches=[]):
        self.label = label
        for branch in branches:
            assert isinstance(branch, Tree)
        self.branches = list(branches)

    def is_leaf(self):
        return not self.branches

    def __repr__(self):
        if self.branches:
            branch_str = ', ' + repr(self.branches)
        else:
            branch_str = ''
        return 'Tree({0}{1})'.format(repr(self.label), branch_str)

    def __str__(self):
        return '\n'.join(self.indented())

    def indented(self):
        lines = []
        for b in self.branches:
            for line in b.indented():
                lines.append('  ' + line)
        return [str(self.label)] + lines

