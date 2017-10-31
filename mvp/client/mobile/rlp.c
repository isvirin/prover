// Blashyrkh.maniac.coding

#include "rlp.h"
#include <stdlib.h>
#include <string.h>
#include <assert.h>


struct rlp_item *rlp_create_string_item(const uint8_t *data, size_t size)
{
    struct rlp_item *item=(struct rlp_item *)malloc(sizeof(struct rlp_item));

    if(!item)
        return NULL;

    item->data=(uint8_t *)malloc(size);
    if(!item->data)
    {
        free(item);
        return NULL;
    }

    item->refcount=1;
    item->parent=NULL;
    item->next=NULL;
    item->firstchild=NULL;
    item->lastchild=NULL;
    item->numchildren=0;
    memcpy(item->data, data, size);
    item->datasize=size;

    return item;
}

struct rlp_item *rlp_create_list_item()
{
    struct rlp_item *item=(struct rlp_item *)malloc(sizeof(struct rlp_item));

    if(!item)
        return NULL;

    item->refcount=1;
    item->parent=NULL;
    item->next=NULL;
    item->firstchild=NULL;
    item->lastchild=NULL;
    item->numchildren=0;
    item->data=NULL;
    item->datasize=0;

    return item;
}

struct rlp_item *rlp_duplicate_item(const struct rlp_item *item)
{
    struct rlp_item *newitem;

    if(!item)
        return NULL;

    newitem=(struct rlp_item *)malloc(sizeof(struct rlp_item));
    if(!newitem)
        return NULL;

    newitem->refcount=1;
    newitem->parent=NULL;
    newitem->next=NULL;
    newitem->firstchild=NULL;
    newitem->lastchild=NULL;
    newitem->numchildren=0;
    newitem->data=NULL;
    newitem->datasize=0;

    if(item->data)
    {
        newitem->data=(uint8_t *)malloc(item->datasize);
        // TODO: check for NULL
        memcpy(newitem->data, item->data, item->datasize);
        newitem->datasize=item->datasize;
    }
    else
    {
        struct rlp_item **pp=&newitem->firstchild;
        struct rlp_item *p;

        for(p=item->firstchild; p; p=p->next)
        {
            struct rlp_item *child=rlp_duplicate_item(p);
            // TODO: check for NULL
            child->parent=newitem;

            *pp=child;
            pp=&child->next;

            ++newitem->numchildren;
            newitem->lastchild=child;
        }
    }

    return newitem;
}

struct rlp_item *rlp_get_item(struct rlp_item *item)
{
    if(item)
        ++item->refcount;
    return item;
}

void rlp_put_item(struct rlp_item *item)
{
    if(item && --item->refcount==0)
    {
        if(item->data)
        {
            free(item->data);
        }
        else
        {
            struct rlp_item *child=item->firstchild;
            while(child)
            {
                struct rlp_item *nextchild=child->next;
                rlp_put_item(child);
                child=nextchild;
            }
        }

        free(item);
    }
}

static size_t estimate_int_length(size_t n)
{
    size_t res=0;
    while(n>0)
    {
        n>>=8;
        ++res;
    }
    return res;
}

static void encode_length(size_t n, uint8_t *buffer_right_end)
{
    while(n>0)
    {
        *buffer_right_end=n&0xFF;
        --buffer_right_end;
        n>>=8;
    }
}

size_t rlp_get_serialized_size(const struct rlp_item *item)
{
    if(item->data && item->datasize==1 && item->data[0]<=0x7F)
        return 1;
    else if(item->data && item->datasize<=55)
        return 1+item->datasize;
    else if(item->data)
        return 1+estimate_int_length(item->datasize)+item->datasize;
    else
    {
        size_t totalPayload=0;
        struct rlp_item *child;

        for(child=item->firstchild; child; child=child->next)
            totalPayload+=rlp_get_serialized_size(child);

        if(totalPayload<=55)
            return 1+totalPayload;
        else
            return 1+estimate_int_length(totalPayload)+totalPayload;
    }
}

static size_t rlp_serialize_to(const struct rlp_item *item, uint8_t *buffer)
{
    if(item->data && item->datasize==1 && item->data[0]<=0x7F)
    {
        *buffer=item->data[0];
        return 1;
    }
    else if(item->data && item->datasize<=55)
    {
        *buffer=0x80+item->datasize;
        memcpy(buffer+1, item->data, item->datasize);
        return 1+item->datasize;
    }
    else if(item->data)
    {
        size_t l=estimate_int_length(item->datasize);

        *buffer=0xB7+l;
        encode_length(item->datasize, buffer+l);

        memcpy(buffer+1+l, item->data, item->datasize);

        return 1+l+item->datasize;
    }
    else
    {
        size_t totalPayload=0;
        struct rlp_item *child;
        uint8_t *p;

        for(child=item->firstchild; child; child=child->next)
            totalPayload+=rlp_get_serialized_size(child);

        if(totalPayload<=55)
        {
            *buffer=0xC0+totalPayload;

            p=buffer+1;
            for(child=item->firstchild; child; child=child->next)
            {
                p+=rlp_serialize_to(child, p);
            }

            return 1+totalPayload;
        }
        else
        {
            size_t l=estimate_int_length(totalPayload);

            *buffer=0xF7+l;
            encode_length(totalPayload, buffer+l);

            p=buffer+1+l;
            for(child=item->firstchild; child; child=child->next)
            {
                p+=rlp_serialize_to(child, p);
            }

            return 1+l+totalPayload;
        }
    }
}

size_t rlp_serialize(const struct rlp_item *item, uint8_t **buffer)
{
    size_t size=rlp_get_serialized_size(item);
    *buffer=(uint8_t *)malloc(size);
    if(!*buffer)
        return 0;

    return rlp_serialize_to(item, *buffer);
}

struct rlp_item *rlp_list_append_item(struct rlp_item *parent, struct rlp_item *child)
{
    struct rlp_item *p;

    if(!parent || !child)
        return NULL;

    // PARENT must not be a child of a CHILD
    p=parent;
    while(p)
    {
        if(p==child)
            return NULL;

        p=p->parent;
    }

    // Nothing changed?
    if(child->parent==parent)
        return child;

    // Detach from the old parent
    if(child->parent)
    {
        if(child->parent->firstchild==child && child->parent->lastchild==child)
        {
            child->parent->firstchild=NULL;
            child->parent->lastchild=NULL;
        }
        else if(child->parent->firstchild==child)
        {
            child->parent->firstchild=child->next;
        }
        else
        {
            struct rlp_item *prevchild=child->parent->firstchild;
            while(prevchild)
            {
                if(prevchild->next==child)
                    break;
                prevchild=prevchild->next;
            }

            assert(prevchild);

            prevchild->next=child->next;
            if(!prevchild->next)
                prevchild->parent->lastchild=prevchild;
        }

        --child->parent->numchildren;

        child->next=NULL;
        child->parent=NULL;
    }

    // Append to the list of children of the new parent
    child->parent=parent;
    ++parent->numchildren;
    if(parent->lastchild)
    {
        parent->lastchild->next=child;
        parent->lastchild=child;
    }
    else
    {
        parent->firstchild=child;
        parent->lastchild=child;
    }
}

size_t rlp_get_list_item_count(const struct rlp_item *item)
{
    return item?item->numchildren:0;
}


#ifdef TEST_RLP

#include <stdio.h>

int main()
{
    {
        struct rlp_item *p=rlp_create_string_item((const uint8_t *)"dog", 3);
        uint8_t *buffer;
        size_t size, i;

        size=rlp_serialize(p, &buffer);
        for(i=0; i<size; ++i)
            printf("%02X ", buffer[i]);
        printf("\n");
        free(buffer);

        rlp_put_item(p);
    }

    {
        struct rlp_item *p=rlp_create_list_item();
        rlp_list_append_item(p, rlp_create_string_item((const uint8_t *)"cat", 3));
        rlp_list_append_item(p, rlp_create_string_item((const uint8_t *)"dog", 3));
        uint8_t *buffer;
        size_t size, i;

        size=rlp_serialize(p, &buffer);
        for(i=0; i<size; ++i)
            printf("%02X ", buffer[i]);
        printf("\n");
        free(buffer);

        rlp_put_item(p);
    }

    {
        struct rlp_item *p=rlp_create_string_item((const uint8_t *)"", 0);
        uint8_t *buffer;
        size_t size, i;

        size=rlp_serialize(p, &buffer);
        for(i=0; i<size; ++i)
            printf("%02X ", buffer[i]);
        printf("\n");
        free(buffer);

        rlp_put_item(p);
    }

    {
        struct rlp_item *p=rlp_create_list_item();
        uint8_t *buffer;
        size_t size, i;

        size=rlp_serialize(p, &buffer);
        for(i=0; i<size; ++i)
            printf("%02X ", buffer[i]);
        printf("\n");
        free(buffer);

        rlp_put_item(p);
    }

    {
        struct rlp_item *p=rlp_create_string_item((const uint8_t *)"\x0F", 1);
        uint8_t *buffer;
        size_t size, i;

        size=rlp_serialize(p, &buffer);
        for(i=0; i<size; ++i)
            printf("%02X ", buffer[i]);
        printf("\n");
        free(buffer);

        rlp_put_item(p);
    }

    {
        struct rlp_item *p=rlp_create_string_item((const uint8_t *)"\x04\x00", 2);
        uint8_t *buffer;
        size_t size, i;

        size=rlp_serialize(p, &buffer);
        for(i=0; i<size; ++i)
            printf("%02X ", buffer[i]);
        printf("\n");
        free(buffer);

        rlp_put_item(p);
    }

    {
        struct rlp_item *zero, *one, *two, *three;

        zero=rlp_create_list_item();
        one=rlp_create_list_item();
        rlp_list_append_item(one, rlp_duplicate_item(zero));
        two=rlp_create_list_item();
        rlp_list_append_item(two, rlp_duplicate_item(zero));
        rlp_list_append_item(two, rlp_duplicate_item(one));
        three=rlp_create_list_item();
        rlp_list_append_item(three, rlp_duplicate_item(zero));
        rlp_list_append_item(three, rlp_duplicate_item(one));
        rlp_list_append_item(three, rlp_duplicate_item(two));

        uint8_t *buffer;
        size_t size, i;

        size=rlp_serialize(three, &buffer);
        for(i=0; i<size; ++i)
            printf("%02X ", buffer[i]);
        printf("\n");
        free(buffer);

        rlp_put_item(zero);
        rlp_put_item(one);
        rlp_put_item(two);
        rlp_put_item(three);
    }

    {
        struct rlp_item *p=rlp_create_string_item((const uint8_t *)"Lorem ipsum dolor sit amet, consectetur adipisicing elit", 56);
        uint8_t *buffer;
        size_t size, i;

        size=rlp_serialize(p, &buffer);
        for(i=0; i<size; ++i)
            printf("%02X ", buffer[i]);
        printf("\n");
        free(buffer);

        rlp_put_item(p);
    }

    return 0;
}

#endif
