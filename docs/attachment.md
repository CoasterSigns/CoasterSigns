# **Attachment Modification**

- Sign Type `attachment`
- Used to change attachments on passing trains/carts
- Feature Name: `attchMod`
    - Subnames: `apply`, `inline`, `direction`

**Prerequisites:** A rough understanding of how TrainCarts Attachments work.

## Apply a modification

### Config File Syntax

```
[!train]
attachments
apply
(filename)
```

`filename` points to a file in the plugin's `attachments` subfolder excluding `.yml`.

For example, the file at `plugins/CoasterSigns/attachments/lightsOff.yml` would be adressed with `lightsOff`.

See the **modification file syntax** below.

These files do not have to be reloaded, they are always re-read with every execution.

### Inline Syntax

```
[!train]
attachments
(target)
(modification)
```

`target` is made up of a range, and a child target, both of which are optional The range is indicated by starting with `
r`, then providing a range in the same syntax as in the modification file. The child element target is indicated by
starting with `c`, then providing a target in the same syntax as in the modification file.

**Examples:**

- The first child element of the first cart: `r0c0`
- The second child's third child of the first 3 carts: `r..2c1:2`
- The root element of the fifth cart: `r4` ← c is omitted, targetting the root element.
- The first child element of all carts: `c0` ← r is omitted, targetting every cart.
- Targetting the root element of all carts would mean omitting both parts, in which case you need to delcare `inline`.

`modification` can be one of:

- `i=(item id without namespace)` (item),
- `t=(none/empty/item)` (type),
- `m=(int)` (model)

## Movement Direction Filter

```
[!train]
attachments
(first parameter) (direction)
(fourth line)
```

`direction` can be `>`, `<`, `right`, `left`, `north`, `south`, `west`, `east`, `up`, `down` or any of their first
letters.
`>`, `<`, `right` and `left` (only applicable on physical signs) are used on signs to distinguish the train's movement
relative to the sign.

## Modification file syntax:

```yml
modifications:
  # list of individual modifications to make
  - range: .. # every cart, default behaviour
    # this argument is ignored when executing on a single cart
    # change CustomModelData:
    custommodeldata: int
    # exchange displayed item
    item: id, without namespace
    # change attachment type (only requires declaration to change it)
    type: none | empty | item

    # target first child attachment:
    child: 0
    # target fourth child's first child's second child:
    child: 3:0:1
    # to target root attachment, omit child property
  - range: 0 # head cart
    #...
  - range: 2..5 # carts 3 to 6 (inclusive)
    #...
  - range: 3.. # carts 4 and onwards
    #...
  - range: ..2 # every cart up to the third (inclusive)
```