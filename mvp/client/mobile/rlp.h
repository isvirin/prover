// Blashyrkh.maniac.coding

#ifndef __RLP_H
#define __RLP_H

#include <stdint.h>
#include <stddef.h>


struct rlp_item
{
    unsigned int     refcount;
    struct rlp_item *parent;
    struct rlp_item *next;
    struct rlp_item *firstchild;
    struct rlp_item *lastchild;
    unsigned int     numchildren;

    uint8_t         *data;
    size_t           datasize;
};

// Item creation and destruction. A newborn item has reference count equal
// to 1. There are two kinds of items - string (byte array) and list.
struct rlp_item *rlp_create_string_item(const uint8_t *data, size_t size);
struct rlp_item *rlp_create_be_int_item(const uint8_t *data, size_t size);
struct rlp_item *rlp_create_list_item();
struct rlp_item *rlp_duplicate_item(const struct rlp_item *item);

// Increase item reference count by 1
struct rlp_item *rlp_get_item(struct rlp_item *item);
// Decrease item reference count by 1 and free the item if the count reaches
// zero.
void rlp_put_item(struct rlp_item *item);

// Serialize item
size_t rlp_get_serialized_size(const struct rlp_item *item);
size_t rlp_serialize(const struct rlp_item *item, uint8_t **buffer);

// List operations

// Item is appended to the children list of the specified parent. In case of
// success the child item is returned, but reference count is not increased.
// In case of failure NULL is returned.
struct rlp_item *rlp_list_append_item(struct rlp_item *parent, struct rlp_item *child);
size_t rlp_get_list_item_count(const struct rlp_item *item);

#endif
